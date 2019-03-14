package com.vattima.lego.imaging.file;

import com.vattima.lego.imaging.config.LegoImagingProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.common.ImageMetadata;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ImageCollectorTest {

    @Test
    public void test_getImagePaths_getsAllPathsInRootFolder() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:jpg-root")
                                              .toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);
        List<Path> paths = imageCollector.getImagePaths();
        assertThat(paths.size()).isEqualTo(5);
    }

    @Test
    public void test_getImageMetadataItem() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:jpgs/jpeg-with-keywords.jpg")
                                              .toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);
        ImageMetadata m = imageCollector.getImageMetadata.apply(jpgPath.toUri()
                                                                       .toURL());
        assertThat(m).isNotNull();
    }

    @Test
    public void test_jpgImageMetadataItems() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:jpgs/jpeg-with-keywords.jpg")
                                              .toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);
        ImageMetadata m = imageCollector.getImageMetadata.apply(jpgPath.toUri()
                                                                       .toURL());
        Stream<ImageMetadataItem> items = imageCollector.jpgImageMetadataItems.apply(m);
        assertThat(items).isNotEmpty()
                         .hasSize(6);
    }

    @Test
    public void test_jpgImageMetadataItemsKeywords() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:jpgs/jpeg-with-keywords.jpg")
                                              .toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        legoImagingProperties.setKeywordsKeyName("XPKeywords:");
        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);
        ImageMetadata m = imageCollector.getImageMetadata.apply(jpgPath.toUri()
                                                                       .toURL());
        Stream<ImageMetadataItem> items = imageCollector.jpgImageMetadataItems.apply(m);
        assertThat(items.filter(imageCollector.keywordsFilter)).isNotEmpty()
                                                               .hasSize(1);
    }


    @Test
    public void test_keywordsExtractor() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:jpgs/jpeg-with-keywords.jpg")
                                              .toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        legoImagingProperties.setKeywordsKeyName("XPKeywords:");
        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);
        ImageMetadata m = imageCollector.getImageMetadata.apply(jpgPath.toUri()
                                                                       .toURL());
        Stream<ImageMetadataItem> items = imageCollector.jpgImageMetadataItems.apply(m);
        Map<String, String> map = items.filter(imageCollector.keywordsFilter)
                                       .flatMap(imageCollector.keywordsExtractor)
                                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1));
        System.out.println(map);
    }

    @Test
    public void test_actualLegoPhotos() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords")
                                              .toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        legoImagingProperties.setKeywordsKeyName("Keywords:");
        ImageCollector imageCollector = new ImageCollector(legoImagingProperties);
        List<Path> paths = imageCollector.getImagePaths();
        assertThat(paths.size()).isEqualTo(21);
        paths.forEach(p -> {
            try {
                ImageMetadata m = imageCollector.getImageMetadata.apply(p.toUri()
                                                                         .toURL());
                Stream<ImageMetadataItem> items = imageCollector.jpgImageMetadataItems.apply(m);
                Map<String, String> map = items.filter(imageCollector.keywordsFilter)
                                               .flatMap(imageCollector.keywordsExtractor)
                                               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1));
                log.info("path=[{}], map=[{}]", p, map);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }
}