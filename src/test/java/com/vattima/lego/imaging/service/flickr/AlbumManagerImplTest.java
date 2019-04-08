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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

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

        deleteSubDirectoriesInPath(jpgPath);

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

        deleteSubDirectoriesInPath(jpgPath);

        AlbumManager albumManager = new AlbumManagerImpl(imageManager, legoImagingProperties, imageCollector, flickrProperties, bricklinkInventoryDao);
        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0504-missing-uuid.jpg"));
        AlbumManifest albumManifest = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest).isNull();
    }


    @Test
    public void addPhoto_inSameAlbum_returnsCachedAlbumManifest() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords-cache-test")
                .toURI());
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                .getAbsolutePath());
        BricklinkInventoryDao bricklinkInventoryDao = mock(BricklinkInventoryDao.class);
        deleteSubDirectoriesInPath(jpgPath);

        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0505.jpg"));
        AlbumManifest albumManifest1 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest1).isNotNull();

        photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0506.jpg"));
        AlbumManifest albumManifest2 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest2).isNotNull();
        assertThat(albumManifest2).isSameAs(albumManifest1);

        photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0514.jpg"));
        AlbumManifest albumManifest3 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest1).isNotNull();
        assertThat(albumManifest3).isNotIn(albumManifest1, albumManifest2);

        photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0515.jpg"));
        AlbumManifest albumManifest4 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest4).isNotNull();
        assertThat(albumManifest4).isNotIn(albumManifest1, albumManifest2);
        assertThat(albumManifest4).isSameAs(albumManifest3);
    }

    private void deleteSubDirectoriesInPath(Path path) throws IOException {
        Files.walk(path)
                .filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
                .filter(p -> !p.equals(path))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(p -> {
                    final boolean deleted = p.delete();
                    System.out.println("Delete ["+p+"] result --> ["+deleted+"]");
                });
    }
}