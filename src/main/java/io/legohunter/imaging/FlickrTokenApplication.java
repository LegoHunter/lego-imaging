package io.legohunter.imaging;

import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.service.PhotoServiceResponse;
import io.legohunter.imaging.service.flickr.CreatePhotoSetRequest;
import io.legohunter.imaging.service.FlickrPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@EnableConfigurationProperties
@SpringBootApplication(scanBasePackages = {"net.bricklink", "com.bricklink", "com.vattima"})
public class FlickrTokenApplication {
    public static void main(String[] args) {
        SpringApplication.run(LegoImagingApplication.class, args);
    }

//    @Component
    @RequiredArgsConstructor
    private class FlickrTest implements ApplicationRunner {
        private final FlickrPhotoService flickrPhotoService;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            log.info("FlickrPhotoService [{}]", flickrPhotoService);
            AlbumManifest albumManifest = new AlbumManifest();
            albumManifest.setTitle("My test Album");
            albumManifest.setDescription("I created this using the Flickr Java API :-)");
            CreatePhotoSetRequest request = new CreatePhotoSetRequest(albumManifest);
            PhotoServiceResponse response = flickrPhotoService.createAlbum(request);
//            PhotosetsInterface photosetsInterface = new PhotosetsInterface(flickrSecrets.getKey(), flickrSecrets.getSecret(), flickrTransport);
//            Photosets photosets = photosetsInterface.getList(flickrProperties.getUserId());
//            photosets.getPhotosets().forEach(ps -> log.info("photoset=[{}]", ps.getTitle()));
        }
    }
}
