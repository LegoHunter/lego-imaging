package io.legohunter.imaging.flickr.impl;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.IUploader;
import com.flickr4java.flickr.uploader.UploadMetaData;
import io.legohunter.imaging.exception.LegoImagingException;
import io.legohunter.imaging.flickr.api.FlickrPhotoService;
import io.legohunter.imaging.flickr.model.FlickrServiceResponse;
import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.HostedAlbum;
import io.legohunter.imaging.model.HostedAlbumMembershipRequest;
import io.legohunter.imaging.model.HostedPhoto;
import io.legohunter.imaging.model.HostedPhotoMetadataUpdate;
import io.legohunter.imaging.model.PhotoMetaDataV1;
import io.legohunter.imaging.model.PhotoServiceRequest;
import io.legohunter.imaging.model.PhotoServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlickrPhotoServiceImpl implements FlickrPhotoService {
    private static final boolean SYNC_UPLOAD = false;
    private static final String JPEG_MIME_TYPE = "image/jpeg";

    private final IUploader uploader;
    private final PhotosetsInterface photosetsInterface;
    private final PhotosInterface photosInterface;
    private final Auth flickrAuth;

    @Override
    public PhotoServiceResponse<String> uploadPhoto(PhotoServiceRequest<PhotoMetaDataV1> request) {
        PhotoServiceResponse<String> response;
        PhotoMetaDataV1 photoMetaData = request.get();
        try {
            byte[] photoBytes = Files.readAllBytes(photoMetaData.getAbsolutePath());
            String photoId = withFlickrAuth(() -> uploader.upload(photoBytes, uploadMetaData(photoMetaData)));
            response = new FlickrServiceResponse<>(photoId);
        } catch (FlickrException e) {
            response = flickrError(e);
        } catch (IOException e) {
            response = new FlickrServiceResponse<>(e);
        }
        log.debug("Flickr Response [{}]", response);
        return response;
    }

    @Override
    public PhotoServiceResponse<String> replacePhoto(PhotoServiceRequest<PhotoMetaDataV1> request) {
        PhotoServiceResponse<String> response;
        PhotoMetaDataV1 photoMetaData = request.get();
        try {
            byte[] photoBytes = Files.readAllBytes(photoMetaData.getAbsolutePath());
            String photoId = withFlickrAuth(() -> uploader.replace(photoBytes, photoMetaData.getPhotoId(), SYNC_UPLOAD));
            response = new FlickrServiceResponse<>(photoId);
        } catch (FlickrException e) {
            response = flickrError(e);
        } catch (IOException e) {
            response = new FlickrServiceResponse<>(e);
        }
        log.debug("Flickr Response [{}]", response);
        return response;
    }

    @Override
    public PhotoServiceResponse<Void> deletePhoto(PhotoServiceRequest<String> request) {
        PhotoServiceResponse<Void> response;
        try {
            withFlickrAuth(() -> {
                photosInterface.delete(request.get());
                return null;
            });
            response = new FlickrServiceResponse<>((Void) null);
        } catch (FlickrException e) {
            response = flickrError(e);
        }
        log.debug("Flickr Response [{}]", response);
        return response;
    }

    @Override
    public PhotoServiceResponse<HostedAlbum> createAlbum(PhotoServiceRequest<AlbumManifest> request) throws LegoImagingException {
        PhotoServiceResponse<HostedAlbum> response;
        AlbumManifest albumManifest = request.get();
        try {
            if (albumManifest.getPhotos().size() < 1) {
                throw new LegoImagingException("Cannot create a Flickr Album that has no photos");
            }
            PhotoMetaDataV1 primaryPhoto = albumManifest.getPrimaryPhoto();
            Photoset photoset = withFlickrAuth(() -> photosetsInterface.create(
                    albumManifest.getTitle(),
                    albumManifest.getDescription(),
                    primaryPhoto.getPhotoId()));
            response = new FlickrServiceResponse<>(HostedAlbum.builder()
                    .id(photoset.getId())
                    .url(photoset.getUrl())
                    .build());
        } catch (FlickrException e) {
            response = flickrError(e);
        }
        log.debug("Flickr Response [{}]", response);
        return response;
    }

    @Override
    public PhotoServiceResponse<Void> updateAlbumMembership(PhotoServiceRequest<HostedAlbumMembershipRequest> request) {
        PhotoServiceResponse<Void> response;
        HostedAlbumMembershipRequest membershipRequest = request.get();
        try {
            withFlickrAuth(() -> {
                photosetsInterface.editPhotos(
                        membershipRequest.getAlbumId(),
                        membershipRequest.getPrimaryPhotoId(),
                        membershipRequest.getPhotoIds().toArray(String[]::new));
                return null;
            });
            response = new FlickrServiceResponse<>((Void) null);
        } catch (FlickrException e) {
            response = flickrError(e);
        }
        log.debug("Flickr Response [{}]", response);
        return response;
    }

    @Override
    public PhotoServiceResponse<Void> updatePhotoMetadata(PhotoServiceRequest<HostedPhotoMetadataUpdate> request) {
        PhotoServiceResponse<Void> response;
        HostedPhotoMetadataUpdate metadataUpdate = request.get();
        try {
            withFlickrAuth(() -> {
                photosInterface.setMeta(
                        metadataUpdate.getPhotoId(),
                        metadataUpdate.getTitle(),
                        metadataUpdate.getDescription());
                if (metadataUpdate.getTags() != null && !metadataUpdate.getTags().isEmpty()) {
                    photosInterface.setTags(metadataUpdate.getPhotoId(), metadataUpdate.getTags().toArray(String[]::new));
                }
                return null;
            });
            response = new FlickrServiceResponse<>((Void) null);
        } catch (FlickrException e) {
            response = flickrError(e);
        }
        log.debug("Flickr Response [{}]", response);
        return response;
    }

    @Override
    public PhotoServiceResponse<HostedPhoto> getPhoto(PhotoServiceRequest<String> request) {
        PhotoServiceResponse<HostedPhoto> response;
        String photoId = request.get();
        try {
            Photo photo = withFlickrAuth(() -> photosInterface.getPhoto(photoId));
            response = new FlickrServiceResponse<>(HostedPhoto.builder()
                    .id(photo.getId())
                    .title(photo.getTitle())
                    .build());
        } catch (FlickrException e) {
            response = flickrError(e);
        }
        log.debug("Flickr Response [{}]", response);
        return response;
    }

    private UploadMetaData uploadMetaData(PhotoMetaDataV1 photoMetaData) {
        UploadMetaData uploadMetaData = new UploadMetaData();
        uploadMetaData.setAsync(SYNC_UPLOAD);
        uploadMetaData.setContentType(Flickr.CONTENTTYPE_PHOTO);
        uploadMetaData.setFamilyFlag(false);
        uploadMetaData.setFilemimetype(JPEG_MIME_TYPE);
        uploadMetaData.setFilename(photoMetaData.getFilename().toString());
        uploadMetaData.setDescription("Description [" + photoMetaData.getFilename() + "]");
        uploadMetaData.setFriendFlag(false);
        uploadMetaData.setHidden(false);
        uploadMetaData.setPublicFlag(true);
        uploadMetaData.setSafetyLevel(Flickr.SAFETYLEVEL_SAFE);
        uploadMetaData.setTags(Collections.emptyList());
        uploadMetaData.setTitle("Title [" + photoMetaData.getFilename() + "]");
        return uploadMetaData;
    }

    private <T> T withFlickrAuth(FlickrCall<T> call) throws FlickrException {
        RequestContext requestContext = RequestContext.getRequestContext();
        Auth previousAuth = requestContext.getAuth();
        requestContext.setAuth(flickrAuth);
        try {
            return call.execute();
        } finally {
            requestContext.setAuth(previousAuth);
        }
    }

    private <T> PhotoServiceResponse<T> flickrError(FlickrException e) {
        return new FlickrServiceResponse<>(e, e.getErrorCode(), e.getErrorMessage());
    }

    @FunctionalInterface
    private interface FlickrCall<T> {
        T execute() throws FlickrException;
    }
}
