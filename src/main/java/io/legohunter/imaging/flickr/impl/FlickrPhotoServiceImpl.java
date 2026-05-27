package io.legohunter.imaging.flickr.impl;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.photos.Extras;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.IUploader;
import com.flickr4java.flickr.uploader.UploadMetaData;
import io.legohunter.imaging.exception.LegoImagingException;
import io.legohunter.imaging.flickr.api.FlickrPhotoService;
import io.legohunter.imaging.flickr.model.FlickrServiceResponse;
import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.HostedAlbum;
import io.legohunter.imaging.model.HostedAlbumPage;
import io.legohunter.imaging.model.HostedAlbumPhotoSearchRequest;
import io.legohunter.imaging.model.HostedAlbumSearchRequest;
import io.legohunter.imaging.model.HostedAlbumMembershipRequest;
import io.legohunter.imaging.model.HostedAlbumMetadataUpdate;
import io.legohunter.imaging.model.HostedPhoto;
import io.legohunter.imaging.model.HostedPhotoMetadataUpdate;
import io.legohunter.imaging.model.HostedPhotoPage;
import io.legohunter.imaging.model.PhotoMetaDataV1;
import io.legohunter.imaging.model.PhotoServiceErrorType;
import io.legohunter.imaging.model.PhotoServiceRequest;
import io.legohunter.imaging.model.PhotoServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
            response = new FlickrServiceResponse<>(e, FlickrServiceResponse.INTERNAL_ERROR_CODE, e.getMessage(), PhotoServiceErrorType.VALIDATION_FAILED);
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
            response = new FlickrServiceResponse<>(e, FlickrServiceResponse.INTERNAL_ERROR_CODE, e.getMessage(), PhotoServiceErrorType.VALIDATION_FAILED);
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
    public PhotoServiceResponse<HostedAlbumPage> listAlbums(PhotoServiceRequest<HostedAlbumSearchRequest> request) {
        PhotoServiceResponse<HostedAlbumPage> response;
        HostedAlbumSearchRequest searchRequest = request.get();
        try {
            Photosets photosets = withFlickrAuth(() -> photosetsInterface.getList(
                    searchRequest.getUserId(),
                    positiveOrZero(searchRequest.getPerPage()),
                    positiveOrZero(searchRequest.getPage()),
                    null
            ));
            response = new FlickrServiceResponse<>(HostedAlbumPage.builder()
                    .albums(Optional.ofNullable(photosets)
                            .map(Photosets::getPhotosets)
                            .orElse(Collections.emptyList()).stream()
                            .map(this::toHostedAlbum)
                            .toList())
                    .page(photosets == null ? 0 : photosets.getPage())
                    .pages(photosets == null ? 0 : photosets.getPages())
                    .perPage(photosets == null ? 0 : photosets.getPerPage())
                    .total(photosets == null ? 0 : photosets.getTotal())
                    .build());
        } catch (FlickrException e) {
            response = flickrError(e);
        }
        log.debug("Flickr Response [{}]", response);
        return response;
    }

    @Override
    public PhotoServiceResponse<HostedPhotoPage> listAlbumPhotos(PhotoServiceRequest<HostedAlbumPhotoSearchRequest> request) {
        PhotoServiceResponse<HostedPhotoPage> response;
        HostedAlbumPhotoSearchRequest searchRequest = request.get();
        try {
            PhotoList<Photo> photos = withFlickrAuth(() -> photosetsInterface.getPhotos(
                    searchRequest.getAlbumId(),
                    Set.of(Extras.DATE_UPLOAD, Extras.URL_M, Extras.URL_O),
                    Flickr.PRIVACY_LEVEL_NO_FILTER,
                    positiveOrZero(searchRequest.getPerPage()),
                    positiveOrZero(searchRequest.getPage())
            ));
            response = new FlickrServiceResponse<>(HostedPhotoPage.builder()
                    .photos(Optional.ofNullable(photos)
                            .map(PhotoList::stream)
                            .orElseGet(Stream::empty)
                            .map(this::toHostedPhoto)
                            .toList())
                    .page(photos == null ? 0 : photos.getPage())
                    .pages(photos == null ? 0 : photos.getPages())
                    .perPage(photos == null ? 0 : photos.getPerPage())
                    .total(photos == null ? 0 : photos.getTotal())
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
    public PhotoServiceResponse<Void> updateAlbumMetadata(PhotoServiceRequest<HostedAlbumMetadataUpdate> request) {
        PhotoServiceResponse<Void> response;
        HostedAlbumMetadataUpdate metadataUpdate = request.get();
        try {
            withFlickrAuth(() -> {
                photosetsInterface.editMeta(
                        metadataUpdate.getAlbumId(),
                        metadataUpdate.getTitle(),
                        metadataUpdate.getDescription());
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

    private HostedAlbum toHostedAlbum(Photoset photoset) {
        return HostedAlbum.builder()
                .id(photoset.getId())
                .url(photoset.getUrl())
                .title(photoset.getTitle())
                .description(photoset.getDescription())
                .primaryPhotoId(photoset.getPrimaryPhoto() == null ? null : photoset.getPrimaryPhoto().getId())
                .photoCount(photoset.getPhotoCount())
                .build();
    }

    private HostedPhoto toHostedPhoto(Photo photo) {
        return HostedPhoto.builder()
                .id(photo.getId())
                .title(photo.getTitle())
                .description(photo.getDescription())
                .url(photo.getUrl())
                .primary(photo.isPrimary())
                .build();
    }

    private int positiveOrZero(Integer value) {
        if (value == null || value < 1) {
            return 0;
        }
        return value;
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
        return new FlickrServiceResponse<>(e, e.getErrorCode(), e.getErrorMessage(), flickrErrorType(e));
    }

    private PhotoServiceErrorType flickrErrorType(FlickrException e) {
        String code = e.getErrorCode();
        String message = normalize(e.getErrorMessage());
        if (message.contains("photoset not found")) {
            return PhotoServiceErrorType.ALBUM_NOT_FOUND;
        }
        if (message.contains("primary") && message.contains("not found")) {
            return PhotoServiceErrorType.PRIMARY_PHOTO_NOT_FOUND;
        }
        if (message.contains("photo not found")) {
            return PhotoServiceErrorType.PHOTO_NOT_FOUND;
        }
        if ("100".equals(code) || message.contains("invalid api key")) {
            return PhotoServiceErrorType.INVALID_API_KEY;
        }
        if (Set.of("95", "96", "97", "98").contains(code) || message.contains("auth token") || message.contains("signature")) {
            return PhotoServiceErrorType.AUTHENTICATION_FAILED;
        }
        if ("99".equals(code) || message.contains("permission") || message.contains("not logged in")) {
            return PhotoServiceErrorType.AUTHORIZATION_FAILED;
        }
        if ("105".equals(code) || message.contains("service unavailable") || message.contains("temporarily unavailable")) {
            return PhotoServiceErrorType.SERVICE_UNAVAILABLE;
        }
        if (message.contains("rate limit") || message.contains("throttle")) {
            return PhotoServiceErrorType.RATE_LIMITED;
        }
        if (message.contains("network") || message.contains("timeout") || message.contains("timed out")) {
            return PhotoServiceErrorType.NETWORK_ERROR;
        }
        if ("106".equals(code) || message.contains("write failed") || message.contains("write operation")) {
            return PhotoServiceErrorType.WRITE_FAILED;
        }
        if (message.contains("file too large")) {
            return PhotoServiceErrorType.FILE_TOO_LARGE;
        }
        if (message.contains("upload limit")) {
            return PhotoServiceErrorType.UPLOAD_LIMIT_EXCEEDED;
        }
        if (message.contains("duplicate")) {
            return PhotoServiceErrorType.DUPLICATE_UPLOAD;
        }
        if (message.contains("empty") && message.contains("photo")) {
            return PhotoServiceErrorType.EMPTY_PHOTO_LIST;
        }
        if (message.contains("invalid")) {
            return PhotoServiceErrorType.VALIDATION_FAILED;
        }
        return PhotoServiceErrorType.UNKNOWN;
    }

    private String normalize(String message) {
        return Optional.ofNullable(message)
                .map(value -> value.trim().toLowerCase())
                .orElse("");
    }

    @FunctionalInterface
    private interface FlickrCall<T> {
        T execute() throws FlickrException;
    }
}
