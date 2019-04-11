package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.file.ImageCollector;
import com.vattima.lego.imaging.flickr.configuration.FlickrProperties;
import com.vattima.lego.imaging.model.AlbumManifest;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
        Optional<AlbumManifest> albumManifest = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest).isNotEmpty();
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
        Optional<AlbumManifest> albumManifest = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest).isEmpty();
    }

    @Test
    public void addPhoto_inSameAlbum_returnsCachedAlbumManifest() throws Exception {
        Path jpgPath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords-cache-test")
                                              .toURI());
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        BricklinkInventoryDao bricklinkInventoryDao = mock(BricklinkInventoryDao.class);
        deleteSubDirectoriesInPath(jpgPath);

        Optional<AlbumManifest> emptyAlbumManifest = albumManager.getAlbumManifest("bogus");
        assertThat(emptyAlbumManifest).isEmpty();

        PhotoMetaData photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0505.jpg"));
        Optional<AlbumManifest> albumManifest1 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest1).isNotEmpty();
        String uuid = photoMetaData.getKeyword("uuid");
        Optional<AlbumManifest> albumManifestWithUuid = albumManager.getAlbumManifest(uuid);
        assertThat(albumManifestWithUuid).isNotEmpty();
        assertThat(albumManifestWithUuid.get()).isSameAs(albumManifest1.get());

        photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0506.jpg"));
        Optional<AlbumManifest> albumManifest2 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest2.get()).isSameAs(albumManifest1.get());

        photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0514.jpg"));
        Optional<AlbumManifest> albumManifest3 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest3).isNotEmpty();
        assertThat(albumManifest3.get()).isNotIn(albumManifest1, albumManifest2);

        photoMetaData = new PhotoMetaData(jpgPath.resolve("DSC_0515.jpg"));
        Optional<AlbumManifest> albumManifest4 = albumManager.addPhoto(photoMetaData);
        assertThat(albumManifest4).isNotEmpty();
        ;
        assertThat(albumManifest4.get()).isNotIn(albumManifest1, albumManifest2);
        assertThat(albumManifest4.get()).isSameAs(albumManifest3.get());
    }

    @Test
    public void addPhoto_hasChangedPhotos_updatesAlbumManifest_andReplacesPhotos() throws Exception {
        Path rootTestImagePath = Paths.get(ResourceUtils.getURL("classpath:actual-lego-photos-with-keywords-changed-md5-test")
                                                        .toURI());
        Path existingPath = rootTestImagePath.resolve(Paths.get("existing"));
        Path changedPath = rootTestImagePath.resolve(Paths.get("changed"));

        deleteSubDirectoriesInPath(existingPath);
        legoImagingProperties.setRootImagesFolder(existingPath.toFile()
                                                              .getAbsolutePath());

        Set<AlbumManifest> albumManifests = new HashSet<>();
        Files.walk(Paths.get(legoImagingProperties.getRootImagesFolder()))
             .filter(p -> p.getFileName()
                           .toString()
                           .endsWith(".JPG"))
             .map(PhotoMetaData::new)
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
             .map(PhotoMetaData::new)
             .forEach(pmd -> {
                 System.out.println("Processing [" + pmd.getAbsolutePath() + "]");
                 imageManager.extractKeywords(pmd);
                 String uuid = pmd.getKeyword("uuid");
                 Optional<AlbumManifest> albumManifest = albumManager.getAlbumManifest(uuid);
                 assertThat(albumManifest).isNotEmpty();
                 List<PhotoMetaData> photos = albumManifest.map(AlbumManifest::getPhotos)
                                                           .orElseGet(Collections::emptyList);

                 Optional<AlbumManifest> albumManifestWithChangedPhotos = albumManager.addPhoto(pmd);
                 Optional<PhotoMetaData> photoMetaData = albumManifestWithChangedPhotos.get()
                                                                                       .getPhotoByFilename(pmd.getFilename());
                 assertThat(photoMetaData.get().getUploadedTimeStamp()).isNull();
                 assertThat(photoMetaData.get().getUploadReturnCode()).isEqualTo(-1);
                 assertThat(photoMetaData.get().isChanged()).isTrue();
                 photoMetaData.map(p -> assertThat(p).isIn(photos))
                              .orElseGet(() -> fail("Filename [" + pmd.getPath() + "] should have been found in AlbumManifest"));
             });
    }

    private void deleteSubDirectoriesInPath(Path path) throws IOException {
        Files.walk(path)
             .filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
             .filter(p -> !p.equals(path))
             .forEach(this::deleteDirectory);
    }

    private void deleteDirectory(Path path) {
        try {
            Files.walk(path)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(p -> {
                     final boolean deleted = p.delete();
                     System.out.println("Delete [" + p + "] result --> [" + deleted + "]");
                 });
        } catch (IOException e) {
            throw new LegoImagingException(e);
        }
    }

    private Path renamed(Path oldPathToFile) {
        System.out.println("Calling renamed");
        Pattern pattern = Pattern.compile("(.*DSC_[0-9]+?)-changed\\.(.*)");
        Matcher matcher = pattern.matcher(oldPathToFile.getFileName()
                                                       .toString());
        if (matcher.matches()) {
            Path newPathToFile = oldPathToFile.getParent()
                                              .resolve(matcher.group(1) + "." + matcher.group(2));
            try {
                Files.move(oldPathToFile, newPathToFile);
                System.out.println("Renamed file from [" + oldPathToFile + "] to [" + newPathToFile + "]");
                return newPathToFile;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Could not rename file [" + oldPathToFile + "]");
        }
    }
}