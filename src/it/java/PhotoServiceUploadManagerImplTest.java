import com.flickr4java.flickr.Transport;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.IUploader;
import com.vattima.lego.imaging.TestApplication;
import com.vattima.lego.imaging.api.bitly.BitlinksAPI;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.flickr.configuration.FlickrConfiguration;
import com.vattima.lego.imaging.flickr.configuration.FlickrProperties;
import com.vattima.lego.imaging.service.AlbumManager;
import com.vattima.lego.imaging.service.ImageManager;
import com.vattima.lego.imaging.service.PhotoServiceUploadManagerImpl;
import com.vattima.lego.imaging.service.bitly.BitlinksService;
import com.vattima.lego.imaging.service.flickr.ImageManagerImpl;
import com.vattima.lego.imaging.test.MockFlickerIUploader;
import com.vattima.lego.imaging.test.MockFlickerPhotosetsInterface;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.ibatis.configuration.MybatisConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Slf4j
@TestPropertySource(locations = "classpath:application.yml")
@ContextConfiguration(classes = {PhotoServiceUploadManagerImplTest.TestConfig.class, TestApplication.class, BricklinkInventoryDao.class, FlickrConfiguration.class, MybatisConfiguration.class, PhotoServiceUploadManagerImplTest.TestConfig.class})
class PhotoServiceUploadManagerImplTest {

    @Autowired
    FlickrProperties flickrProperties;

    @Autowired
    IUploader uploader;

    @Autowired
    PhotosetsInterface photosetsInterface;

    @Autowired
    BricklinkInventoryDao bricklinkInventoryDao;

    BitlinksService bitlinksService;

    @Mock
    BitlinksAPI bitlinksAPI;

    private LegoImagingProperties legoImagingProperties;
    private ImageManager imageManager;
    private AlbumManager albumManager;

    @BeforeEach
    void setup() {
        legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setKeywordsKeyName("Keywords:");
        imageManager = new ImageManagerImpl();
    }

    @Test
    void dummy() {
        BitlinksService bitlinksService = new BitlinksService(bitlinksAPI);
        PhotoServiceUploadManagerImpl photoServiceUploadManager = new PhotoServiceUploadManagerImpl(photosetsInterface, uploader, legoImagingProperties, albumManager, bitlinksService);
    }

    @TestConfiguration
    static class TestConfig {

        @Primary
        @Bean
        public IUploader testUploader() {
            return new MockFlickerIUploader();

        }

        @Primary
        @Bean
        public PhotosetsInterface testPhotosetsInterface(FlickrProperties.Secrets flickrSecrets, Transport flickrTransport) {
            return new MockFlickerPhotosetsInterface(flickrSecrets.getKey(), flickrSecrets.getSecret(), flickrTransport);
        }
    }
}