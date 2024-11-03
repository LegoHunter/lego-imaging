package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.AlbumManager;
import com.vattima.lego.imaging.service.ImageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.util.stream.Stream.Builder;

@Slf4j
@RequiredArgsConstructor
@Component
public class AlbumManagerImpl implements AlbumManager {
    private final ImageManager imageManager;
    private final LegoImagingProperties legoImagingProperties;
    private final BricklinkInventoryDao bricklinkInventoryDao;

    private Map<String, AlbumManifest> albums = new ConcurrentHashMap<>();

    public Stream<AlbumManifest> findManifests(Path path) {
        try {
            Builder<AlbumManifest> builder = Stream.builder();
            Files.walkFileTree(path, new AlbumManifestFileVisitor(builder));
            return builder.build();
        } catch (IOException e) {
            throw new LegoImagingException(e);
        }
    }

    @Override
    public AlbumManifest readAlbumManifest(Path path) {
        return AlbumManifest.fromJson(path);
    }

    @Override
    public void writeAlbumManifest(AlbumManifest albumManifest) {
        AlbumManifest.toJson(albumManifest.getAlbumManifestFile(legoImagingProperties.getRootImagesPath()), albumManifest);
    }

    @Override
    public Optional<AlbumManifest> addPhoto(PhotoMetaData photoMetaData) {
        AlbumManifest albumManifest;

        // Extract the keywords from the PhotoMetaData
        imageManager.getKeywords(photoMetaData);

        // get uuid and blItemNumber
        String uuid = photoMetaData.getKeyword("uuid");
        if (null == uuid) {
            log.warn("Photo [{}] does not contain keyword uuid - photo will not be added to AlbumManifest", photoMetaData.getAbsolutePath());
            return Optional.empty();
        }
        String blItemNumber = photoMetaData.getKeyword("bl");
        if (null == blItemNumber) {
            log.warn("Photo [{}] does not contain keyword bl - photo will not be added to AlbumManifest", photoMetaData.getAbsolutePath());
            return Optional.empty();
        }

        albumManifest = getAlbumManifest(uuid, blItemNumber);

        // compute MD5 hash of the photo
        String md5Hash = photoMetaData.getMd5();

        // get Photo by filename from AlbumManifest
        Optional<PhotoMetaData> photoMetaDataInAlbum = albumManifest.getPhotoByFilename(photoMetaData.getFilename());
        if (photoMetaDataInAlbum.isPresent()) {
            String existingMd5Hash = photoMetaDataInAlbum.get()
                                                         .getMd5();
            if (md5Hash.equals(existingMd5Hash)) {
                // Photo has not changed, no need to upload
                log.info("MD5 hash has not changed - no upload necessary [{}]", photoMetaDataInAlbum.get());
            } else {
                // Upload changed photo
                PhotoMetaData oldPhotoMetaData = photoMetaDataInAlbum.get();
                photoMetaData.setPhotoId(oldPhotoMetaData.getPhotoId());
                photoMetaData.setPrimary(photoMetaData.getPrimary());
                photoMetaData.setMd5(md5Hash);
                photoMetaData.setUploadedTimeStamp(null);
                photoMetaData.setUploadReturnCode(-1);
                photoMetaData.setChanged(true);
                albumManifest.getPhotos()
                             .remove(oldPhotoMetaData);
                albumManifest.getPhotos()
                             .add(photoMetaData);
                movePhoto(photoMetaData);
                log.info("MD5 hash has changed from [{}] to [{}] - upload necessary [{}]", photoMetaDataInAlbum.get()
                                                                                                               .getMd5(), photoMetaData.getMd5(), photoMetaData);
            }
        } else {
            // Add photo to manifest and upload it.
            photoMetaData.setChanged(true);
            albumManifest.getPhotos()
                         .add(photoMetaData);
            movePhoto(photoMetaData);
            log.info("Added photo [{}] to album manifest [{}]", photoMetaData, albumManifest);
            log.info("New Photo - upload necessary [{}]", photoMetaData);
        }

        // Update the database with information garnered from the photo keywords.
        BricklinkInventory bricklinkInventory = BricklinkInventory.fromKeywords(photoMetaData.getKeywords());
        bricklinkInventoryDao.updateFromImageKeywords(bricklinkInventory);
        bricklinkInventory = bricklinkInventoryDao.getByUuid(albumManifest.getUuid());
        albumManifest.updateFromBricklinkInventory(bricklinkInventory);

        // save AlbumManifest
        writeAlbumManifest(albumManifest);

        return Optional.of(albumManifest);
    }

    @Override
    public Optional<AlbumManifest> movePhoto(PhotoMetaData photoMetaData) {
        Optional<AlbumManifest> albumManifest = Optional.ofNullable(albums.get(photoMetaData.getKeyword("uuid")));
        albumManifest.ifPresent(a -> {
            Path targetPath = a.getAlbumManifestPath(Path.of(legoImagingProperties.getRootImagesFolder()));
            photoMetaData.move(targetPath);
        });
        return albumManifest;
    }

    @Override
    public AlbumManifest getAlbumManifest(String uuid, String blItemNumber) {
        AlbumManifest albumManifest;

        // if uuid is a key in the map, get AlbumManifest out of the Map.
        if (albums.containsKey(uuid)) {
            albumManifest = albums.get(uuid);
            log.info("Found AlbumManifest in cache [{}]", albumManifest);
        } else {
            // Get Path to AlbumManifest file
            Path albumManifestPath = AlbumManifest.getAlbumManifestFile(legoImagingProperties.getRootImagesPath(), uuid, blItemNumber);

            // check for existence of AlbumManifest json file
            if (Files.exists(albumManifestPath)) {
                // If it exists, read it into an AlbumManifest
                albumManifest = readAlbumManifest(albumManifestPath);
                log.info("Read AlbumManifest [{}] from file [{}]", albumManifest, albumManifestPath);
            } else {
                // else if it doesn't exist, create an empty AlbumManifest
                albumManifest = new AlbumManifest();
                albumManifest.setUuid(uuid);
                albumManifest.setBlItemNumber(blItemNumber);
                albumManifest.setNew(true);
                log.info("Created new AlbumManifest [{}]", albumManifest);
            }
            // add AlbumManifest to the map
            albums.put(uuid, albumManifest);
            log.info("Put AlbumManifest [{}] in cache", albumManifest);
        }
        return albumManifest;
    }

    @RequiredArgsConstructor
    private static class AlbumManifestFileVisitor extends SimpleFileVisitor<Path> {
        private final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*-manifest.json");
        private final Builder<AlbumManifest> builder;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (matcher.matches(file.getFileName())) {
                builder.add(AlbumManifest.fromJson(file));
            }
            return CONTINUE;
        }
    }
}
