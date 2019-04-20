package com.vattima.lego.imaging.service;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.IUploader;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class PhotoServiceUploadManagerImpl implements PhotoServiceUploadManager {
    private final PhotosetsInterface photosetsInterface;
    private final IUploader uploader;
    private final LegoImagingProperties legoImagingProperties;

    private Set<AlbumManifest> albumManifests = new HashSet<>();

    @Override
    public void queue(AlbumManifest albumManifest) {
        albumManifests.add(albumManifest);
    }

    @Override
    public void updateAll() {
        final Path root = Paths.get(legoImagingProperties.getRootImagesFolder());
        albumManifests.forEach(a -> {
            // Upload all new/changed photos
            a.getPhotos()
             .forEach(pmd -> {
                 try {
                     if (Optional.ofNullable(pmd.getPhotoId())
                                 .isPresent()) {
                         if (pmd.isChanged()) {
                             log.info("Uploading changed photo [{}]", pmd);
                             StopWatch timer = new StopWatch();
                             timer.start();
                             String response = uploader.replace(Files.readAllBytes(pmd.getAbsolutePath()), pmd.getPhotoId(), false);
                             timer.stop();
                             pmd.setPhotoId(response);
                             log.info("Uploaded changed photo [{}] in [{}] ms", pmd, timer.getTotalTimeMillis());
                         } else {
                             log.info("Photo not changed [{}]", pmd);
                         }
                     } else {
                         UploadMetaData uploadMetaData = new UploadMetaData();
                         uploadMetaData.setAsync(false);
                         uploadMetaData.setContentType(Flickr.CONTENTTYPE_PHOTO);
                         uploadMetaData.setFamilyFlag(false);
                         uploadMetaData.setFilemimetype("image/jpeg");
                         uploadMetaData.setFilename(pmd.getFilename()
                                                       .toString());
                         uploadMetaData.setDescription("Description [" + pmd.getFilename()
                                                                            .toString() + "]");
                         uploadMetaData.setFriendFlag(false);
                         uploadMetaData.setHidden(false);
                         uploadMetaData.setPublicFlag(true);
                         uploadMetaData.setSafetyLevel(Flickr.SAFETYLEVEL_SAFE);
                         uploadMetaData.setTags(Collections.emptyList());
                         uploadMetaData.setTitle("Title [" + pmd.getFilename()
                                                                .toString() + "]");
                         log.info("Uploading new [{}]", pmd);
                         StopWatch timer = new StopWatch();
                         timer.start();
                         String response = uploader.upload(Files.readAllBytes(pmd.getAbsolutePath()), uploadMetaData);
                         timer.stop();
                         pmd.setUploadReturnCode(0);
                         pmd.setUploadedTimeStamp(LocalDateTime.now());
                         pmd.setPhotoId(response);
                         log.info("Uploaded new [{}] in [{}] ms", pmd, timer.getTotalTimeMillis());
                     }
                 } catch (FlickrException | IOException e) {
                     pmd.setUploadReturnCode(-1);
                     throw new LegoImagingException(e);
                 }
             });
            // Create the Album if it doesn't exist
            String photosetId = Optional.ofNullable(a.getPhotosetId())
                    .orElseGet(() -> {
                        try {
                            PhotoMetaData primaryPhoto = a.getPrimaryPhoto();
                            String primaryPhotoId = primaryPhoto.getPhotoId();
                            Photoset photoset = photosetsInterface.create(a.getTitle(), a.getDescription(), primaryPhotoId);
                            log.info("Created Photoset [{}] with primary photo id [{}] - filename [{}]", photoset, primaryPhoto.getFilename(), primaryPhotoId);
                            a.setPhotosetId(photoset.getId());
                            photosetsInterface.editPhotos(a.getPhotosetId(), primaryPhotoId, a.getPhotoIdsArray());
                            log.info("Updated Photoset [{}] with primary photo id [{}] - added photos {}", photoset, primaryPhoto.getFilename(), a.getPhotoIdsArray());
                            AlbumManifest.toJson(a.getAlbumManifestFile(root), a);
                            return a.getPhotosetId();
                        } catch (FlickrException e) {
                            throw new LegoImagingException(e);
                        }
                    });
        });
        albumManifests.clear();
    }
}
