package com.vattima.lego.imaging;

import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.flickr.configuration.FlickrProperties;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.AlbumManager;
import com.vattima.lego.imaging.service.PhotoServiceUploadManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Files;

@EnableConfigurationProperties
@SpringBootApplication(scanBasePackages = {"net.bricklink", "com.bricklink", "com.vattima"})
@Slf4j
public class LegoImagingApplication {


    public static void main(String[] args) {
        SpringApplication.run(LegoImagingApplication.class, args);
    }

    @Component
    @RequiredArgsConstructor
    private class ImageRunner implements ApplicationRunner {
        private final FlickrProperties flickrProperties;
        private final AlbumManager albumManager;
        private final LegoImagingProperties legoImagingProperties;
        private final PhotoServiceUploadManager photoServiceUploadManager;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            log.info("Flickr Properties: [{}]", flickrProperties);
            Files.newDirectoryStream(legoImagingProperties.getRootImagesPath(), "*.jpg")
                 .forEach(p -> {
                     PhotoMetaData photoMetaData = new PhotoMetaData(p.getParent(), p.getFileName());
                     albumManager.addPhoto(photoMetaData);
                 });
            photoServiceUploadManager.updateAll();
        }
    }

}
