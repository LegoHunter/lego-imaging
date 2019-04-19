package com.vattima.lego.imaging;

import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.file.ImageCollector;
import com.vattima.lego.imaging.flickr.configuration.FlickrProperties;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.ImageFileHolder;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.AlbumManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

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
        private final ImageCollector imageCollector;
        private final LegoImagingProperties legoImagingProperties;
        private final FlickrProperties flickrProperties;
        private final BricklinkInventoryDao bricklinkInventoryDao;
        private final AlbumManager albumManager;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            log.info("Flickr Properties: [{}]", flickrProperties);
            imageCollector.getImagePaths()
                          .forEach(p -> {
                              PhotoMetaData photoMetaData = new PhotoMetaData(p.getParent(), p.getFileName());
                              ImageFileHolder imageFileHolder = new ImageFileHolder(p, imageCollector, bricklinkInventoryDao);
                              imageFileHolder.updateInventoryFromKeywords();
                              albumManager.addPhoto(photoMetaData);
                              //imageFileHolder.move();
                          });
        }
    }

}
