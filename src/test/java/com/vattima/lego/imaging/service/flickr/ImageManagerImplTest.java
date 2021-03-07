package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.ImageManager;
import com.vattima.lego.imaging.util.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.junit.jupiter.api.Test;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ImageManagerImplTest {
    @Test
    void getKeywords_withJpgFileThatHasKeywords() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("jpgs", "jpeg-with-keywords.jpg");
        ImageManagerImpl imageManager = new ImageManagerImpl();
        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath);
        Map<String, String> map = imageManager.getKeywords(photoMetaData);
        assertThat(map).isNotNull();
        assertThat(map.keySet()).hasSize(4);
        assertThat(map).containsOnlyKeys("tag1", "tag2", "b", "tag3");
        assertThat(map).containsEntry("tag1", "a");
        assertThat(map).containsEntry("tag2", "tag2");
        assertThat(map).containsEntry("b", "b");
        assertThat(map).containsEntry("tag3", "123");
    }

    @Test
    void metaDataExtractor_extracts() throws Exception {

        Path jpgPath = PathUtils.fromClasspath("lego-photo-with-metadata", "DSC_3368.JPG");
        List<? extends ImageMetadata.ImageMetadataItem> metadataItems = Imaging.getMetadata(jpgPath.toFile())
                                                                               .getItems();
        ImageManager imageManager = new ImageManagerImpl();
        Map<String, String> map = imageManager.getKeywords(new PhotoMetaData(jpgPath));
        log.info("map = [{}]", map);
    }

    @Test
    void getKeywords_withJpgFileThatHasNoKeywords_returnsEmptyMap() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("jpgs", "jpeg-without-keywords.jpg");
        ImageManagerImpl imageManager = new ImageManagerImpl();
        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath);
        Map<String, String> map = imageManager.getKeywords(photoMetaData);
        assertThat(map).isNotNull();
        assertThat(map.keySet()).hasSize(0);
    }

    @Test
    void getMetadata_withCopyrightTitleCaption() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("lego-photo-with-metadata");
        ImageManagerImpl imageManager = new ImageManagerImpl();
        DirectoryStream<Path> paths = Files.newDirectoryStream(jpgPath, "DSC_3368.JPG");
        paths.forEach(p -> {
            try {
                PhotoMetaData photoMetaData = new PhotoMetaData(p);
                Map<String, String> map = imageManager.getKeywords(photoMetaData);
                assertThat(map).containsKey("cp");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void getKeywords_withMultiWordKeyword_doesntSplitKeyword() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("lego-photo-multi-word-keyword");
        ImageManagerImpl imageManager = new ImageManagerImpl();
        DirectoryStream<Path> paths = Files.newDirectoryStream(jpgPath, "IMG_20191016_214600.jpg");
        paths.forEach(p -> {
            try {
                PhotoMetaData photoMetaData = new PhotoMetaData(p);
                Map<String, String> map = imageManager.getKeywords(photoMetaData);
                assertThat(map).isNotNull();
                assertThat(map.keySet()).isNotNull();
                assertThat(map.keySet()
                              .size()).isGreaterThan(0);
                log.info("path=[{}], map=[{}]", p, map);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void getKeywords_withJpgFilesThatHaveKeywords_returnANonEmptyMap() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("actual-lego-photos-with-keywords");
        ImageManagerImpl imageManager = new ImageManagerImpl();
        DirectoryStream<Path> paths = Files.newDirectoryStream(jpgPath, "*.jpg");
        paths.forEach(p -> {
            try {
                PhotoMetaData photoMetaData = new PhotoMetaData(p);
                Map<String, String> map = imageManager.getKeywords(photoMetaData);
                assertThat(map).isNotNull();
                assertThat(map.keySet()).isNotNull();
                assertThat(map.keySet()
                              .size()).isGreaterThan(0);
                log.info("path=[{}], map=[{}]", p, map);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}