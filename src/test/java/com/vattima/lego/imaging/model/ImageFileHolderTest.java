package com.vattima.lego.imaging.model;

import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.file.ImageCollector;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageFileHolderTest {

    @Test
    public void getKeywords() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:jpgs/jpeg-with-keywords.jpg")
                                              .toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setKeywordsKeyName("XPKeywords:");
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);

        ImageFileHolder imageFileHolder = new ImageFileHolder(jpgPath, imageCollector, null);
        assertThat(imageFileHolder.getKeywords()).isNotEmpty();
        assertThat(imageFileHolder.getKeywords()
                                  .entrySet())
                .containsOnly(javaMapEntry("b", "b"),
                        javaMapEntry("tag1", "a"),
                        javaMapEntry("tag2", "tag2"),
                        javaMapEntry("tag3", "123"));
    }

    @Test
    public void uuid() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords").toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setKeywordsKeyName("Keywords:");
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile().getAbsolutePath());
        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);
        ImageFileHolder imageFileHolder = new ImageFileHolder(Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords/DSC_0504.JPG").toURI()), imageCollector, null);
        assertThat(imageFileHolder.hasUuid()).isTrue();
        assertThat(imageFileHolder.getUuid()).isEqualTo("fdaa0638814727a42f005656f38b92c6");

    }

    @Test
    public void bricklinkItem() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords").toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setKeywordsKeyName("Keywords:");
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile().getAbsolutePath());
        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);
        ImageFileHolder imageFileHolder = new ImageFileHolder(Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords/DSC_0504.JPG").toURI()), imageCollector, null);
        assertThat(imageFileHolder.hasBricklinkItemNumber()).isTrue();
        assertThat(imageFileHolder.getBricklinkItemNumber()).isEqualTo("6658-1");

    }
//
//    @Test
//    public void getMd5Hash() throws Exception {
//        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords").toURI());
//        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
//        legoImagingProperties.setKeywordsKeyName("Keywords:");
//        legoImagingProperties.setRootImagesFolder(jpgPath.toFile().getAbsolutePath());
//        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);
//        ImageFileHolder imageFileHolder = new ImageFileHolder(Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords/DSC_0504.JPG").toURI()), imageCollector, null, legoImagingProperties);
//        imageFileHolder.getMD5Hash(Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords/DSC_0504.JPG").toURI()));
//    }

    private static <K, V> Map.Entry<K, V> javaMapEntry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}