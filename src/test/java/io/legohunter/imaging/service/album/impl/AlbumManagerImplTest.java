package io.legohunter.imaging.service.album.impl;

import io.legohunter.imaging.config.LegoImagingProperties;
import io.legohunter.imaging.metadata.impl.ImageManagerImpl;
import io.legohunter.imaging.flickr.config.FlickrProperties;
import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.PhotoMetaDataV1;
import io.legohunter.imaging.service.album.api.AlbumManager;
import io.legohunter.imaging.metadata.api.ImageManager;
import io.legohunter.imaging.service.upload.api.PhotoServiceUploadManager;
import io.legohunter.imaging.test.UnitTestUtils;
import io.legohunter.imaging.util.PathUtils;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlbumManagerImplTest {
    private LegoImagingProperties legoImagingProperties;
    private ImageManager imageManager;
    private AlbumManager albumManager;
    private FlickrProperties flickrProperties;
    private BricklinkInventoryDao bricklinkInventoryDao;
    private PhotoServiceUploadManager photoServiceUploadManager;

    @BeforeEach
    void setup() {
        flickrProperties = new FlickrProperties();
        legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setKeywordsKeyName("Keywords:");
        bricklinkInventoryDao = mock(BricklinkInventoryDao.class);
        photoServiceUploadManager = mock(PhotoServiceUploadManager.class);
        imageManager = new ImageManagerImpl();
        albumManager = new AlbumManagerImpl(imageManager, legoImagingProperties, bricklinkInventoryDao);
    }

    @Test
    void findManifests_returnsNonEmptyStream() throws Exception {
        Path path = PathUtils.fromClasspath("album-manager-test/find-manifests-test");
        Stream<AlbumManifest> albumManifests = albumManager.findManifests(path);
        assertThat(albumManifests).isNotEmpty().hasSize(6);
    }

    @Test
    void addPhoto() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("album-manager-test/add-photo-test");
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        when(bricklinkInventoryDao.getByUuid(any(String.class))).thenReturn(new BricklinkInventory());

        UnitTestUtils.deleteSubDirectoriesInPath(jpgPath);

        PhotoMetaDataV1 photoMetaData = new PhotoMetaDataV1(jpgPath.resolve("DSC_0504.JPG"));
        Optional<AlbumManifest> albumManifest = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest).isNotEmpty();
    }


    @Test
    void addPhoto_withNoUuid_returnsNull() throws Exception {
        FlickrProperties flickrProperties = new FlickrProperties();

        Path jpgPath = Path.of(ResourceUtils.getURL("classpath:actual-lego-photos-with-keyword-issues")
                                              .toURI());
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        bricklinkInventoryDao = mock(BricklinkInventoryDao.class);

        UnitTestUtils.deleteSubDirectoriesInPath(jpgPath);

        AlbumManager albumManager = new AlbumManagerImpl(imageManager, legoImagingProperties, bricklinkInventoryDao);
        PhotoMetaDataV1 photoMetaData = new PhotoMetaDataV1(jpgPath.resolve("DSC_0504-missing-uuid.JPG"));
        Optional<AlbumManifest> albumManifest = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest).isEmpty();
    }

    @Test
    void addPhoto_inSameAlbum_returnsCachedAlbumManifest() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("actual-lego-photos-with-keywords-cache-test");
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile().getAbsolutePath());
        UnitTestUtils.deleteSubDirectoriesInPath(jpgPath);

        when(bricklinkInventoryDao.getByUuid(any(String.class))).thenReturn(new BricklinkInventory());

        AlbumManifest emptyAlbumManifest = albumManager.getAlbumManifest("bogus", "1234-1");
        assertThat(emptyAlbumManifest).isNotNull();

        PhotoMetaDataV1 photoMetaData = new PhotoMetaDataV1(jpgPath.resolve("DSC_0505.JPG"));
        Optional<AlbumManifest> albumManifest1 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest1).isNotEmpty();
        String uuid = photoMetaData.getKeyword("uuid");
        AlbumManifest albumManifestWithUuid = albumManager.getAlbumManifest(uuid, "1234-1");
        assertThat(albumManifestWithUuid).isNotNull();
        assertThat(albumManifestWithUuid).isSameAs(albumManifest1.get());

        photoMetaData = new PhotoMetaDataV1(jpgPath.resolve("DSC_0506.JPG"));
        Optional<AlbumManifest> albumManifest2 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest2.get()).isSameAs(albumManifest1.get());

        photoMetaData = new PhotoMetaDataV1(jpgPath.resolve("DSC_0514.JPG"));
        Optional<AlbumManifest> albumManifest3 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest3).isNotEmpty();
        assertThat(albumManifest3.get()).isNotIn(albumManifest1, albumManifest2);

        photoMetaData = new PhotoMetaDataV1(jpgPath.resolve("DSC_0515.JPG"));
        Optional<AlbumManifest> albumManifest4 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest4).isNotEmpty();

        assertThat(albumManifest4.get()).isNotIn(albumManifest1, albumManifest2);
        assertThat(albumManifest4.get()).isSameAs(albumManifest3.get());
        verify(bricklinkInventoryDao, times(4)).updateFromImageKeywords(any(BricklinkInventory.class));
    }

    @Test
    void addPhoto_hasChangedPhotos_updatesAlbumManifest_andReplacesPhotos() throws Exception {
        Path rootTestImagePath = PathUtils.fromClasspath("actual-lego-photos-with-keywords-changed-md5-test");
        Path existingPath = rootTestImagePath.resolve(Path.of("existing"));
        Path changedPath = rootTestImagePath.resolve(Path.of("changed"));

        UnitTestUtils.deleteSubDirectoriesInPath(existingPath);
        legoImagingProperties.setRootImagesFolder(existingPath.toFile()
                                                              .getAbsolutePath());
        when(bricklinkInventoryDao.getByUuid(any(String.class))).thenReturn(new BricklinkInventory());

        Set<AlbumManifest> albumManifests = new HashSet<>();
        Files.walk(Path.of(legoImagingProperties.getRootImagesFolder()))
             .filter(p -> p.getFileName()
                           .toString()
                           .endsWith(".JPG"))
             .map(PhotoMetaDataV1::new)
             .forEach(pmd -> {
                 albumManifests.add(albumManager.addPhoto(pmd)
                                                .get());
             });
        assertThat(albumManifests).isNotNull()
                                  .hasSize(2);

        Files.walk(changedPath)
             .filter(p -> p.getFileName()
                           .toString()
                           .endsWith(".JPG"))
             .map(PhotoMetaDataV1::new)
             .forEach(pmd -> {
                 System.out.println("Processing [" + pmd.getAbsolutePath() + "]");
                 imageManager.getKeywords(pmd);
                 String uuid = pmd.getKeyword("uuid");
                 AlbumManifest albumManifest = albumManager.getAlbumManifest(uuid, "1234-1");
                 assertThat(albumManifest).isNotNull();
                 List<PhotoMetaDataV1> photos = albumManifest.getPhotos();

                 Optional<AlbumManifest> albumManifestWithChangedPhotos = albumManager.addPhoto(pmd);
                 Optional<PhotoMetaDataV1> photoMetaData = albumManifestWithChangedPhotos.get()
                                                                                       .getPhotoByFilename(pmd.getFilename());
                 assertThat(photoMetaData.get()
                                         .getUploadedTimeStamp()).isNull();
                 assertThat(photoMetaData.get()
                                         .getUploadReturnCode()).isEqualTo(-1);
                 assertThat(photoMetaData.get()
                                         .isChanged()).isTrue();
                 photoMetaData.map(p -> assertThat(p).isIn(photos))
                              .orElseGet(() -> fail("Filename [" + pmd.getPath() + "] should have been found in AlbumManifest"));
             });
    }
}