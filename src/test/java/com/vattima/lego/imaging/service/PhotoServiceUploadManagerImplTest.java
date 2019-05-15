package com.vattima.lego.imaging.service;

import com.flickr4java.flickr.Transport;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.IUploader;
import com.vattima.lego.imaging.TestApplication;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.flickr.configuration.FlickrConfiguration;
import com.vattima.lego.imaging.flickr.configuration.FlickrProperties;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.flickr.AlbumManagerImpl;
import com.vattima.lego.imaging.service.flickr.ImageManagerImpl;
import com.vattima.lego.imaging.test.MockFlickerIUploader;
import com.vattima.lego.imaging.test.MockFlickerPhotosetsInterface;
import com.vattima.lego.imaging.test.UnitTestUtils;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.ibatis.configuration.IbatisConfiguration;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RunWith(SpringRunner.class)
@Slf4j
@TestPropertySource(locations = "classpath:application.yml")
@ContextConfiguration(classes = {TestApplication.class, BricklinkInventoryDao.class, FlickrConfiguration.class, IbatisConfiguration.class, PhotoServiceUploadManagerImplTest.TestConfig.class}, initializers = ConfigFileApplicationContextInitializer.class)
public class PhotoServiceUploadManagerImplTest {

    @Autowired
    FlickrProperties flickrProperties;

    @Autowired
    IUploader uploader;

    @Autowired
    PhotosetsInterface photosetsInterface;

    @Autowired
    BricklinkInventoryDao bricklinkInventoryDao;

    private LegoImagingProperties legoImagingProperties;
    private ImageManager imageManager;
    private AlbumManager albumManager;

    @Before
    public void setup() {
        legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setKeywordsKeyName("Keywords:");
        imageManager = new ImageManagerImpl();
    }

    @Test
    public void dummy() {

    }

    @Configuration
    static class TestConfig {

        @Primary
        @Bean
        public IUploader uploader() {
            return new MockFlickerIUploader();

        }
        @Primary
        @Bean
        public PhotosetsInterface photosetsInterface(FlickrProperties.Secrets flickrSecrets, Transport flickrTransport) {
            return new MockFlickerPhotosetsInterface(flickrSecrets.getKey(), flickrSecrets.getSecret(), flickrTransport);
        }
    }
}