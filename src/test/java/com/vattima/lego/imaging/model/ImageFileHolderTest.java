package com.vattima.lego.imaging.model;

import static org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

public class ImageFileHolderTest {

    @Test
    public void extractKeywords() throws Exception {
        Path jpgPath = Paths.get(ClassLoader.getSystemResource("jpgs/jpeg-with-keywords.jpg").toURI());
        ImageFileHolder imageFileHolder = new ImageFileHolder(jpgPath);
        imageFileHolder.extractKeywords(p -> Stream.empty());
        assertThat(imageFileHolder.getKeywords()).isNotEmpty();
        assertThat(imageFileHolder.getKeywords().entrySet()).containsOnly(javaMapEntry("k", "v"));
    }

    private static <K, V> Map.Entry<K, V> javaMapEntry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}