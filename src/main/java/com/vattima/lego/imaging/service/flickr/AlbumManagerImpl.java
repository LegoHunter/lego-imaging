package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.file.ImageCollector;
import com.vattima.lego.imaging.flickr.configuration.FlickrProperties;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.ImageFileHolder;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.AlbumManager;
import com.vattima.lego.imaging.service.ImageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class AlbumManagerImpl implements AlbumManager {
    private final ImageManager imageManager;
    private final LegoImagingProperties legoImagingProperties;
    private final ImageCollector imageCollector;
    private final FlickrProperties flickrProperties;
    private final BricklinkInventoryDao bricklinkInventoryDao;

    private Map<String, AlbumManifest> albums = new ConcurrentHashMap<>();

    @Override
    public AlbumManifest addPhoto(PhotoMetaData photoMetaData) {
        AlbumManifest albumManifest;

        // Extract the keywords from the PhotoMetaData
        imageManager.extractKeywords(photoMetaData);

        // get uuid and blItemNumber
        String uuid = photoMetaData.getKeyword("uuid");
        if (null == uuid) {
            log.warn("Photo [{}] does not contain uuid - photo will not be added to AlbumManifest", photoMetaData.getPath());
            return null;
        }
        String blItemNumber = photoMetaData.getKeyword("bl");

        // get target path for photo
        Path targetPath = getAlbumManifestPath(photoMetaData);
        try {
            Files.createDirectories(targetPath);
        } catch (IOException e) {
            throw new LegoImagingException(e);
        }

        // Get Path to AlbumManifest file
        Path albumManifestPath = getAlbumManifestFile(photoMetaData);

        // if uuid is a key in the map, get AlbumManifest out of the Map.
        if (albums.containsKey(uuid)) {
            albumManifest = albums.get(uuid);
            log.info("Found AlbumManifest in cache [{}]", albumManifest);
        } else {
            // check for existence of AlbumManifest json file
            if (Files.exists(albumManifestPath)) {
                // If it exists, read it into an AlbumManifest
                albumManifest = AlbumManifest.fromJson(albumManifestPath);
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

        // compute MD5 hash of the photo
        String md5Hash = imageManager.computeMD5Hash(photoMetaData);

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
                albumManifest.getPhotos().remove(photoMetaDataInAlbum.get());
                albumManifest.getPhotos().add(photoMetaData);
                log.info("MD5 hash has changed from [{}] to [{}] - upload necessary [{}]", photoMetaDataInAlbum.get().getMd5(), photoMetaData.getMd5(), photoMetaData);
            }
        } else {
            // Add photo to manifest and upload it.
            albumManifest.getPhotos()
                         .add(photoMetaData);
            log.info("Added photo [{}] to album manifest [{}]", photoMetaData, albumManifest);
            log.info("New Photo - upload necessary [{}]", photoMetaData);
        }

        // save AlbumManifest
        AlbumManifest.toJson(albumManifestPath, albumManifest);

        return albumManifest;
    }

    public Path getAlbumManifestPath(PhotoMetaData photoMetaData) {
        return Paths.get(legoImagingProperties.getRootImagesFolder() + "/" + photoMetaData.getKeyword("bl") + "-" + photoMetaData.getKeyword("uuid"));
    }

    public Path getAlbumManifestFile(PhotoMetaData photoMetaData) {
        return Paths.get(getAlbumManifestPath(photoMetaData) + "/" + photoMetaData.getKeyword("bl") + "-" + photoMetaData.getKeyword("uuid") + "-manifest.json");
    }

    @Override
    public AlbumManifest uploadToPhotoService(PhotoMetaData photoMetaData, AlbumManifest albumManifest) {
        return null;
    }

    @Override
    public AlbumManifest movePhoto(PhotoMetaData photoMetaData) {
        if (!photoMetaData.canMove()) {
            log.warn("Cannot move image file [{}] - missing required keywords", photoMetaData.getPath());
        } else {
            try {
                Path targetPath = getAlbumManifestPath(photoMetaData);
                if (!Files.exists(targetPath, LinkOption.NOFOLLOW_LINKS)) {
                    Files.createDirectory(targetPath);
                }
                Files.move(photoMetaData.getPath(), Paths.get(targetPath + "/" + photoMetaData.getFilename()));
            } catch (IOException e) {
                throw new LegoImagingException(e);
            }
        }
        return null;
    }
}
