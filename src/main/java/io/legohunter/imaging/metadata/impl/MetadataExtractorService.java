package io.legohunter.imaging.metadata.impl;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import io.legohunter.imaging.metadata.model.ConditionEnum;
import io.legohunter.imaging.metadata.model.ImageMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class MetadataExtractorService {

    public ImageMetadata extractMetadata(byte[] bytes) {
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            Metadata metadata = ImageMetadataReader.readMetadata(is);
            Map<String, String> keywords = new HashMap<>();
            String caption = null;

            for (Directory dir : metadata.getDirectories()) {
                for (Tag tag : dir.getTags()) {
                    String tagName = tag.getTagName();

                    if (isKeywordTag(tagName)) {
                        keywords.putAll(parseKeywordString(tag.getDescription()));
                    }

                    if (caption == null && isCaptionTag(tagName)) {
                        String candidate = trimToNull(tag.getDescription());

                        if (isLikelyHumanCaption(candidate)) {
                            caption = candidate;
                        }
                    }
                }
            }

            ImageMetadata imageMetadata = toImageMetadata(keywords, caption);

            log.debug(
                    "metadata.extracted keywords={} captionPresent={} uuidPresent={} externalItemPresent={}",
                    keywords.size(),
                    imageMetadata.hasCaption(),
                    imageMetadata.hasUuid(),
                    imageMetadata.hasExternalItemNumber()
            );

            return imageMetadata;
        } catch (Exception e) {
            throw new RuntimeException("Image metadata extraction failed", e);
        }
    }

    public String calculateMd5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytes);
            return Hex.encodeHexString(digest);
        } catch (Exception e) {
            throw new RuntimeException("MD5 calculation failed", e);
        }
    }

    private boolean isKeywordTag(String tagName) {
        if (tagName == null) return false;

        String name = tagName.toLowerCase();

        return name.contains("keyword"); // catches IPTC + XMP variants
    }

    private boolean isCaptionTag(String tagName) {
        if (tagName == null) return false;

        String name = tagName.toLowerCase();

        return name.contains("caption")
                || "image description".equals(name)
                || "description".equals(name)
                || name.endsWith(":description");
    }

    ImageMetadata toImageMetadata(Map<String, String> keywords, String caption) {
        return new ImageMetadata(
                trimToNull(keywordValue(keywords, "uuid").orElse(null)),
                trimToNull(keywordValue(keywords, "bl").orElse(null)),
                parseBooleanKeyword(keywords, "primary"),
                parseBooleanKeyword(keywords, "sealed"),
                parseBooleanKeyword(keywords, "bo", "built_once", "builtonce", "built-once"),
                parseConditionKeyword(keywords, "bc", "box_condition", "boxcondition", "box-condition"),
                parseConditionKeyword(keywords, "ic", "instructions_condition", "instructionscondition", "instructions-condition"),
                parseConditionKeyword(keywords, "item", "item_condition", "itemcondition", "item-condition"),
                trimToNull(caption)
        );
    }

    Map<String, String> parseKeywordString(String raw) {

        Map<String, String> result = new HashMap<>();

        if (raw == null || raw.isBlank()) {
            return result;
        }

        String[] pairs = raw.split(";");

        for (String pair : pairs) {

            String trimmed = pair.trim();

            if (trimmed.isBlank()) {
                continue;
            }

            int idx = trimmed.indexOf(':');

            // Presence-only flag
            if (idx < 0) {

                result.put(
                        trimmed.toLowerCase(),
                        "true"
                );

                continue;
            }

            // Malformed
            if (idx == 0) {
                continue;
            }

            String key =
                    trimmed.substring(0, idx)
                            .trim()
                            .toLowerCase();

            String value =
                    trimmed.substring(idx + 1)
                            .trim();

            result.put(key, value);
        }

        return result;
    }

    Boolean parseBooleanKeyword(Map<String, String> keywords, String... keys) {
        Optional<String> value = keywordValue(keywords, keys);

        if (value.isEmpty()) {
            return null;
        }

        String normalizedValue = value.get().trim().toLowerCase();

        if (normalizedValue.isEmpty()) {
            return null;
        }

        if (isTrueValue(normalizedValue, keys)) {
            return true;
        }

        if (isFalseValue(normalizedValue)) {
            return false;
        }

        throw new IllegalArgumentException(
                "Invalid boolean metadata value [%s] for keyword [%s]"
                        .formatted(value.get(), keys[0])
        );
    }

    ConditionEnum parseConditionKeyword(Map<String, String> keywords, String... keys) {
        Optional<String> value = keywordValue(keywords, keys);

        if (value.isEmpty() || value.get().isBlank()) {
            return null;
        }

        return ConditionEnum.requireFromCode(value.get());
    }

    private Optional<String> keywordValue(Map<String, String> keywords, String... keys) {
        if (keywords == null || keys == null) {
            return Optional.empty();
        }

        for (String key : keys) {
            String value = keywords.get(normalizeKey(key));

            if (value != null) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    private boolean isTrueValue(String normalizedValue, String... keys) {
        if ("true".equals(normalizedValue)
                || "yes".equals(normalizedValue)
                || "y".equals(normalizedValue)
                || "1".equals(normalizedValue)
                || "on".equals(normalizedValue)) {
            return true;
        }

        for (String key : keys) {
            if (normalizedValue.equals(normalizeKey(key))) {
                return true;
            }
        }

        return false;
    }

    private boolean isFalseValue(String normalizedValue) {
        return "false".equals(normalizedValue)
                || "no".equals(normalizedValue)
                || "n".equals(normalizedValue)
                || "0".equals(normalizedValue)
                || "off".equals(normalizedValue);
    }

    private boolean isLikelyHumanCaption(String value) {
        if (value == null) {
            return false;
        }

        return value.chars().anyMatch(Character::isLetter);
    }

    private String normalizeKey(String key) {
        return key == null ? null : key.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
