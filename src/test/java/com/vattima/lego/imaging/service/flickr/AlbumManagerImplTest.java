package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.file.ImageCollector;
import com.vattima.lego.imaging.flickr.configuration.FlickrProperties;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.ImageFileHolder;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.AlbumManager;
import com.vattima.lego.imaging.service.ImageManager;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AlbumManagerImplTest {
    private LegoImagingProperties legoImagingProperties;
    private ImageCollector imageCollector;
    private ImageManager imageManager;
    private AlbumManager albumManager;
    private FlickrProperties flickrProperties;
    private BricklinkInventoryDao bricklinkInventoryDao;

    @Before
    public void setup() {
        flickrProperties = new FlickrProperties();
        legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setKeywordsKeyName("Keywords:");
        bricklinkInventoryDao = mock(BricklinkInventoryDao.class);
        imageCollector = new ImageCollector(legoImagingProperties);
        imageManager = new ImageManagerImpl(legoImagingProperties);
        albumManager = new AlbumManagerImpl(imageManager, legoImagingProperties, imageCollector, flickrProperties, bricklinkInventoryDao);

    }

    @Test
    public void addPhoto() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords")
                .toURI());
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                .getAbsolutePath());
        bricklinkInventoryDao = mock(BricklinkInventoryDao.class);

        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0504.jpg"));
        AlbumManifest albumManifest = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest).isNotNull();
    }


    @Test
    public void addPhoto_withNoUuid_returnsNull() throws Exception {
        FlickrProperties flickrProperties = new FlickrProperties();

        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keyword-issues")
                .toURI());
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                .getAbsolutePath());
        bricklinkInventoryDao = mock(BricklinkInventoryDao.class);

        AlbumManager albumManager = new AlbumManagerImpl(imageManager, legoImagingProperties, imageCollector, flickrProperties, bricklinkInventoryDao);
        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0504-missing-uuid.jpg"));
        AlbumManifest albumManifest = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest).isNull();
    }


    @Test
    public void addPhoto_inSameAlbum_returnsCachedAlbumManifest() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords")
                .toURI());
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                .getAbsolutePath());
        BricklinkInventoryDao bricklinkInventoryDao = mock(BricklinkInventoryDao.class);

        ImageFileHolder imageFileHolder = new ImageFileHolder(Paths.get("."), imageCollector, bricklinkInventoryDao);
        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0504.jpg"));
        AlbumManifest albumManifest1 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest1).isNotNull();

        photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0505.jpg"));
        AlbumManifest albumManifest2 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest2).isNotNull();
        assertThat(albumManifest2).isSameAs(albumManifest1);
    }
}