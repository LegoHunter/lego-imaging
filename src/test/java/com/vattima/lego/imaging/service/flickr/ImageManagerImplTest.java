package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.ImageManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.common.ImageMetadata;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ImageManagerImplTest {

    @Test
    public void computeMD5Hash() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords/DSC_0504.JPG")
                                              .toURI());
        ImageManager imageManager = new ImageManagerImpl("Keywords:");
        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath);
        String md5Hash = imageManager.computeMD5Hash(photoMetaData);
        assertThat(md5Hash).isEqualTo("23BFB61B48D367368A03CBC0028C38EB");
    }

    @Test
    public void test_getImagePaths_getsAllPathsInRootFolder() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:jpg-root")
                                              .toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        ImageManagerImpl imageManager = new ImageManagerImpl(legoImagingProperties.getKeywordsKeyName());
        DirectoryStream<Path> paths = imageManager.imagePaths(Paths.get(legoImagingProperties.getRootImagesFolder()));
        AtomicInteger pathCount = new AtomicInteger(0);
        paths.forEach(p -> {
            pathCount.incrementAndGet();
        });
        assertThat(pathCount.get()).isEqualTo(5);
    }

    @Test
    public void test_getImageMetadataItem() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:jpgs/jpeg-with-keywords.jpg")
                                              .toURI());
        LegoImagingProperties legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        ImageManagerImpl imageManager = new ImageManagerImpl("Keywords:");
        ImageMetadata m = imageManager.getImageMetadata.apply(jpgPath.toUri()
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
        ImageManagerImpl imageManager = new ImageManagerImpl("Keywords:");
        ImageMetadata m = imageManager.getImageMetadata.apply(jpgPath.toUri()
                                                                     .toURL());
        Stream<ImageMetadata.ImageMetadataItem> items = imageManager.jpgImageMetadataItems.apply(m);
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
        ImageManagerImpl imageManager = new ImageManagerImpl(legoImagingProperties.getKeywordsKeyName());
        ImageMetadata m = imageManager.getImageMetadata.apply(jpgPath.toUri()
                                                                     .toURL());
        Stream<ImageMetadata.ImageMetadataItem> items = imageManager.jpgImageMetadataItems.apply(m);
        assertThat(items.filter(imageManager.keywordsFilter)).isNotEmpty()
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
        ImageManagerImpl imageManager = new ImageManagerImpl(legoImagingProperties.getKeywordsKeyName());
        ImageMetadata m = imageManager.getImageMetadata.apply(jpgPath.toUri()
                                                                     .toURL());
        Stream<ImageMetadata.ImageMetadataItem> items = imageManager.jpgImageMetadataItems.apply(m);
        Map<String, String> map = items.filter(imageManager.keywordsFilter)
                                       .flatMap(imageManager.keywordsExtractor)
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
        ImageManagerImpl imageManager = new ImageManagerImpl(legoImagingProperties.getKeywordsKeyName());
        DirectoryStream<Path> paths = imageManager.imagePaths(Paths.get(legoImagingProperties.getRootImagesFolder()));
        AtomicInteger pathCount = new AtomicInteger(0);
        paths.forEach(p -> {
            try {
                pathCount.incrementAndGet();
                ImageMetadata m = imageManager.getImageMetadata.apply(p.toUri()
                                                                       .toURL());
                Stream<ImageMetadata.ImageMetadataItem> items = imageManager.jpgImageMetadataItems.apply(m);
                Map<String, String> map = items.filter(imageManager.keywordsFilter)
                                               .flatMap(imageManager.keywordsExtractor)
                                               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1));
                log.info("path=[{}], map=[{}]", p, map);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertThat(pathCount.get()).isEqualTo(21);
    }

}