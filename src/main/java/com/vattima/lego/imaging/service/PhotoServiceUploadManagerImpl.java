package com.vattima.lego.imaging.service;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.IUploader;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.model.AlbumManifest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private Set<Path> albumManifestPaths = new HashSet<>();

    @Override
    public void queue(Path albumManifestPath) {
        albumManifestPaths.add(albumManifestPath);
    }

    @Override
    public void updateAll() {
        albumManifestPaths.forEach(p -> {
            AlbumManifest albumManifest = AlbumManifest.fromJson(p);

            // Upload all new/changed photos
            albumManifest.getPhotos()
                         .forEach(pmd -> {
                             try {
                                 if (Optional.ofNullable(pmd.getPhotoId())
                                             .isPresent()) {
                                     if (pmd.isChanged()) {
                                         log.info("Uploading [{}]", pmd);
                                         String response = uploader.replace(Files.readAllBytes(pmd.getAbsolutePath()), pmd.getPhotoId(), false);
                                         pmd.setPhotoId(response);
                                         log.info("Uploaded [{}]", pmd);
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
                                     log.info("Uploading [{}]", pmd);
                                     String response = uploader.upload(Files.readAllBytes(pmd.getAbsolutePath()), uploadMetaData);
                                     pmd.setPhotoId(response);
                                     log.info("Uploaded [{}]", pmd);
                                 }
                             } catch (FlickrException | IOException e) {
                                 throw new LegoImagingException(e);
                             }
                         });
            AlbumManifest.toJson(p, albumManifest);
        });
    }
}
