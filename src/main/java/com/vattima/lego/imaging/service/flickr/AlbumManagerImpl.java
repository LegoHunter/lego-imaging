package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.LegoImagingException;
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
    private final LegoImagingProperties legoImagingProperties;
    private final ImageCollector imageCollector;
    private final FlickrProperties flickrProperties;
    private final BricklinkInventoryDao bricklinkInventoryDao;

    private Map<String, AlbumManifest> albums = new ConcurrentHashMap<>();

    @Override
    public AlbumManifest addPhoto(ImageFileHolder imageFileHolder) {
        AlbumManifest albumManifest;

        // read keywords
        Map<String, String> keywords = imageFileHolder.getKeywords();

        // get uuid and blItemNumber
        String uuid = imageFileHolder.getUuid();
        if (null == uuid) {
            log.warn("Photo [{}] does not contain uuid - photo will not be added to AlbumManifest");
            return null;
        }
        String blItemNumber = imageFileHolder.getBricklinkItemNumber();

        // get PhotoMetaData from ImageFileHolder
        PhotoMetaData photoMetaData = imageFileHolder.getPhotoMetaData();

        // get target path for photo
        Path targetPath = imageFileHolder.getTargetDirectory();
        try {
            Files.createDirectories(targetPath);
        } catch (IOException e) {
            throw new LegoImagingException(e);
        }

        // Get Path to AlbumManifest file
        Path albumManifestPath = Paths.get(targetPath + "/" + imageFileHolder.getBricklinkItemNumber() + "-" + imageFileHolder.getUuid() + "-manifest.json");

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
        String md5Hash = imageFileHolder.getMd5Hash();

        // get Photo by filename from AlbumManifest
        Optional<PhotoMetaData> photoMetaDataInAlbum = albumManifest.getPhotoByFilename(imageFileHolder.getPath()
                                                                                                       .getFileName());
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

    @Override
    public AlbumManifest uploadToPhotoService(ImageFileHolder imageFileHolder, AlbumManifest albumManifest) {
        return null;
    }

    @Override
    public AlbumManifest movePhoto(ImageFileHolder imageFileHolder) {
        if (!imageFileHolder.canMove()) {
            log.warn("Cannot move image file [{}] - missing required keywords", imageFileHolder.getPath());
        } else {
            try {
                Path targetPath = Paths.get(legoImagingProperties.getRootInventoryItemsFolder() + "/" + imageFileHolder.getBricklinkItemNumber() + "-" + imageFileHolder.getUuid());
                if (!Files.exists(targetPath, LinkOption.NOFOLLOW_LINKS)) {
                    Files.createDirectory(targetPath);
                }
                Files.move(imageFileHolder.getPath(), Paths.get(targetPath + "/" + imageFileHolder.getPath()
                                                                                                  .getFileName()));
            } catch (IOException e) {
                throw new LegoImagingException(e);
            }
        }
        return null;
    }
}
