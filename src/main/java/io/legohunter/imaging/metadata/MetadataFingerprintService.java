package io.legohunter.imaging.metadata;

import io.legohunter.imaging.metadata.model.ConditionEnum;
import io.legohunter.imaging.metadata.model.ImageMetadata;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.StringJoiner;

@Component
public class MetadataFingerprintService {
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String VERSION = "v1";

    public String calculateHash(ImageMetadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("metadata is required");
        }

        return sha256Hex(canonicalMetadata(metadata));
    }

    private String canonicalMetadata(ImageMetadata metadata) {
        StringJoiner fields = new StringJoiner("\n");
        fields.add(VERSION);
        fields.add("uuid=" + normalizeIdentifier(metadata.uuid()));
        fields.add("externalItemNumber=" + normalizeText(metadata.externalItemNumber()));
        fields.add("primary=" + normalizeBoolean(metadata.primary()));
        fields.add("sealed=" + normalizeBoolean(metadata.sealed()));
        fields.add("builtOnce=" + normalizeBoolean(metadata.builtOnce()));
        fields.add("boxCondition=" + normalizeCondition(metadata.boxCondition()));
        fields.add("instructionsCondition=" + normalizeCondition(metadata.instructionsCondition()));
        fields.add("itemCondition=" + normalizeCondition(metadata.itemCondition()));
        fields.add("caption=" + normalizeText(metadata.caption()));
        return fields.toString();
    }

    private String normalizeIdentifier(String value) {
        return normalizeText(value).toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.trim();
    }

    private String normalizeBoolean(Boolean value) {
        return value == null ? "" : value.toString();
    }

    private String normalizeCondition(ConditionEnum condition) {
        return condition == null ? "" : condition.name();
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return bytesToHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte currentByte : bytes) {
            hex.append(String.format("%02x", currentByte));
        }
        return hex.toString();
    }
}
