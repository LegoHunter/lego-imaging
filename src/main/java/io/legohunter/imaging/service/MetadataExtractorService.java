package io.legohunter.imaging.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Service
public class MetadataExtractorService {

    public Map<String, String> extractKeywords(byte[] bytes) {
        try (InputStream is = new ByteArrayInputStream(bytes)) {

            Metadata metadata = ImageMetadataReader.readMetadata(is);
            Map<String, String> result = new HashMap<>();

            for (Directory dir : metadata.getDirectories()) {
                for (Tag tag : dir.getTags()) {
                    if ("Keywords".equalsIgnoreCase(tag.getTagName())) {
                        parseKeywords(tag.getDescription(), result);
                    }
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Metadata extraction failed", e);
        }
    }

    public String calculateMd5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytes);
            return Hex.encodeHexString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void parseKeywords(String raw, Map<String, String> map) {
        if (raw == null) return;

        for (String part : raw.split(",")) {
            String[] kv = part.trim().split(":");
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
    }
}