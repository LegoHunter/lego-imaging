package com.vattima.lego.imaging.model;

import lombok.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class ImageFileHolder {
    private final Path path;
    Map<String, String> keywords;

    public boolean hasUuid() {
        return keywords.containsKey("uuid");
    }
    public String getUuid() {
        return keywords.get("uuid");
    }
    public void extractKeywords(Function<Path, Stream<? extends ImageMetadataItem>> extractor) {
        keywords = extractor.apply(path).collect(Collectors.toMap(k -> "a", v -> "x"));
    }
}
