package com.vattima.lego.imaging.handler;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;

public class KeywordsExtractor implements MetadataExtractor {
    @Override
    public Map<String, String> apply(ImageMetadataItem imageMetadata) {
        return Stream.of(imageMetadata.toString())
                     .filter(s -> s.startsWith("Keywords:"))
                     .map(s -> s.substring(10))
                     .map(s -> {
                         if (s.contains(":")) {
                             return s.split(":");
                         } else {
                             return new String[]{s, s};
                         }
                     })
                     .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1], (k, v) -> v));

    }
}
