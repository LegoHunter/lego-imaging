package com.vattima.lego.imaging.service;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.IUploader;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.vattima.lego.imaging.TestApplication;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.file.ImageCollector;
import com.vattima.lego.imaging.flickr.configuration.FlickrConfiguration;
import com.vattima.lego.imaging.flickr.configuration.FlickrProperties;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.flickr.AlbumManagerImpl;
import com.vattima.lego.imaging.service.flickr.ImageManagerImpl;
import com.vattima.lego.imaging.test.UnitTestUtils;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Random;

import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@Slf4j
@TestPropertySource(locations = "classpath:application.yml")
@ContextConfiguration(classes = {TestApplication.class, FlickrConfiguration.class}, initializers = ConfigFileApplicationContextInitializer.class)
public class PhotoServiceUploadManagerImplTest {

    @Autowired
    FlickrProperties flickrProperties;

    @Autowired
    IUploader uploader;

    @Autowired
    PhotosetsInterface photosetsInterface;

    private LegoImagingProperties legoImagingProperties;
    private ImageManager imageManager;
    private AlbumManager albumManager;

    @Before
    public void setup() {
        legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setKeywordsKeyName("Keywords:");
        imageManager = new ImageManagerImpl(legoImagingProperties.getKeywordsKeyName());
    }

    @Test
    public void queue() throws Exception {
        log.info("FlickrProperties [{}]", flickrProperties);
        PhotoServiceUploadManager photoServiceUploadManager = new PhotoServiceUploadManagerImpl(photosetsInterface, uploader);
        albumManager = new AlbumManagerImpl(imageManager, legoImagingProperties, photoServiceUploadManager);

        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:lego-photos-upload-test").toURI());
        Path jpgChangedPath = Paths.get(ResourceUtils.getURL("classpath:lego-photos-upload-test-changed").toURI());
        UnitTestUtils.deleteSubDirectoriesInPath(jpgPath);
        UnitTestUtils.deleteSubDirectoriesInPath(jpgChangedPath);
        legoImagingProperties.setRootImagesFolder(jpgPath.toAbsolutePath()
                                                         .toString());

        imageManager.imagePaths(jpgPath)
                    .forEach(p -> {
                        log.info("Found image [{}]", p);
                        PhotoMetaData photoMetaData = new PhotoMetaData(p);
                        Optional<AlbumManifest> albumManifest = albumManager.addPhoto(photoMetaData);
                    });
        albumManager.updatePhotoService();

        imageManager.imagePaths(jpgChangedPath)
                    .forEach(p -> {
                        log.info("Found changed image [{}]", p);
                        PhotoMetaData photoMetaData = new PhotoMetaData(p);
                        Optional<AlbumManifest> albumManifest = albumManager.addPhoto(photoMetaData);
                    });
        albumManager.updatePhotoService();
    }

    @Test
    public void updateAll() {
    }

    @Configuration
    static class TestConfig {

        @Primary
        @Bean
        public IUploader uploader() {

            return new IUploader() {
                Random r = new Random();

                private String getRandom() {
                    return String.valueOf(r.nextInt(100000000 - 10000000) + 10000000);
                }

                @Override
                public String upload(byte[] bytes, UploadMetaData uploadMetaData) throws FlickrException {
                    return getRandom();
                }

                @Override
                public String upload(File file, UploadMetaData uploadMetaData) throws FlickrException {
                    return getRandom();
                }

                @Override
                public String upload(InputStream inputStream, UploadMetaData uploadMetaData) throws FlickrException {
                    return getRandom();
                }

                @Override
                public String replace(InputStream inputStream, String s, boolean b) throws FlickrException {
                    return getRandom();
                }

                @Override
                public String replace(byte[] bytes, String s, boolean b) throws FlickrException {
                    return getRandom();
                }

                @Override
                public String replace(File file, String s, boolean b) throws FlickrException {
                    return getRandom();
                }
            };
        }
    }

}