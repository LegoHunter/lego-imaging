package io.legohunter.imaging.flickr.impl;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.IUploader;
import com.flickr4java.flickr.uploader.UploadMetaData;
import io.legohunter.imaging.flickr.model.FlickrServiceRequest;
import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.HostedAlbum;
import io.legohunter.imaging.model.HostedAlbumMembershipRequest;
import io.legohunter.imaging.model.HostedPhoto;
import io.legohunter.imaging.model.PhotoMetaDataV1;
import io.legohunter.imaging.model.PhotoServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FlickrPhotoServiceTest {
    private IUploader uploader;
    private PhotosetsInterface photosetsInterface;
    private PhotosInterface photosInterface;
    private FlickrPhotoServiceImpl flickrPhotoService;

    @BeforeEach
    void setUp() {
        uploader = mock(IUploader.class);
        photosetsInterface = mock(PhotosetsInterface.class);
        photosInterface = mock(PhotosInterface.class);
        flickrPhotoService = new FlickrPhotoServiceImpl(uploader, photosetsInterface, photosInterface);
    }

    @Test
    void uploadPhoto_uploadsFileWithDefaultPhotoMetadata(@TempDir Path tempDir) throws Exception {
        Path photoPath = tempDir.resolve("photo.jpg");
        Files.write(photoPath, new byte[]{1, 2, 3});
        when(uploader.upload(any(byte[].class), any(UploadMetaData.class))).thenReturn("photo-123");

        PhotoServiceResponse<String> response = flickrPhotoService.uploadPhoto(new FlickrServiceRequest<>(new PhotoMetaDataV1(photoPath)));

        assertThat(response.isError()).isFalse();
        assertThat(response.responseCode()).isZero();
        assertThat(response.get()).isEqualTo("photo-123");
        org.mockito.ArgumentCaptor<UploadMetaData> metadataCaptor = org.mockito.ArgumentCaptor.forClass(UploadMetaData.class);
        verify(uploader).upload(eq(new byte[]{1, 2, 3}), metadataCaptor.capture());
        UploadMetaData metadata = metadataCaptor.getValue();
        assertThat(metadata.isAsync()).isFalse();
        assertThat(metadata.getContentType()).isEqualTo(Flickr.CONTENTTYPE_PHOTO);
        assertThat(metadata.getFilemimetype()).isEqualTo("image/jpeg");
        assertThat(metadata.getFilename()).isEqualTo("photo.jpg");
        assertThat(metadata.isPublicFlag()).isTrue();
        assertThat(metadata.isFriendFlag()).isFalse();
        assertThat(metadata.isFamilyFlag()).isFalse();
        assertThat(metadata.isHidden()).isFalse();
        assertThat(metadata.getSafetyLevel()).isEqualTo(Flickr.SAFETYLEVEL_SAFE);
        assertThat(metadata.getTags()).isEmpty();
    }

    @Test
    void replacePhoto_replacesExistingPhoto(@TempDir Path tempDir) throws Exception {
        Path photoPath = tempDir.resolve("photo.jpg");
        Files.write(photoPath, new byte[]{4, 5, 6});
        PhotoMetaDataV1 photoMetaData = new PhotoMetaDataV1(photoPath);
        photoMetaData.setPhotoId("existing-photo");
        when(uploader.replace(any(byte[].class), eq("existing-photo"), eq(false))).thenReturn("replacement-photo");

        PhotoServiceResponse<String> response = flickrPhotoService.replacePhoto(new FlickrServiceRequest<>(photoMetaData));

        assertThat(response.isError()).isFalse();
        assertThat(response.get()).isEqualTo("replacement-photo");
        verify(uploader).replace(eq(new byte[]{4, 5, 6}), eq("existing-photo"), eq(false));
    }

    @Test
    void deletePhoto_deletesPhoto() throws Exception {
        PhotoServiceResponse<Void> response = flickrPhotoService.deletePhoto(new FlickrServiceRequest<>("photo-123"));

        assertThat(response.isError()).isFalse();
        verify(photosInterface).delete("photo-123");
    }

    @Test
    void createAlbum_createsPhotosetFromAlbumManifest() throws Exception {
        Photoset photoset = new Photoset();
        photoset.setId("album-123");
        photoset.setUrl("https://www.flickr.com/photos/user/albums/album-123");
        when(photosetsInterface.create("album title", "album description", "primary-photo")).thenReturn(photoset);

        AlbumManifest albumManifest = new AlbumManifest();
        albumManifest.setTitle("album title");
        albumManifest.setDescription("album description");
        albumManifest.setPhotos(List.of(primaryPhoto("primary-photo")));

        PhotoServiceResponse<HostedAlbum> response = flickrPhotoService.createAlbum(new FlickrServiceRequest<>(albumManifest));

        assertThat(response.isError()).isFalse();
        assertThat(response.get().getId()).isEqualTo("album-123");
        assertThat(response.get().getUrl()).isEqualTo("https://www.flickr.com/photos/user/albums/album-123");
        verify(photosetsInterface).create("album title", "album description", "primary-photo");
    }

    @Test
    void updateAlbumMembership_editsPhotosetMembership() throws Exception {
        HostedAlbumMembershipRequest request = HostedAlbumMembershipRequest.builder()
                .albumId("album-123")
                .primaryPhotoId("photo-1")
                .photoIds(new LinkedHashSet<>(List.of("photo-1", "photo-2")))
                .build();

        PhotoServiceResponse<Void> response = flickrPhotoService.updateAlbumMembership(new FlickrServiceRequest<>(request));

        assertThat(response.isError()).isFalse();
        verify(photosetsInterface).editPhotos("album-123", "photo-1", new String[]{"photo-1", "photo-2"});
    }

    @Test
    void getPhoto_mapsFlickrPhoto() throws Exception {
        Photo photo = new Photo();
        photo.setId("photo-123");
        photo.setTitle("Photo Title");
        when(photosInterface.getPhoto("photo-123")).thenReturn(photo);

        PhotoServiceResponse<HostedPhoto> response = flickrPhotoService.getPhoto(new FlickrServiceRequest<>("photo-123"));

        assertThat(response.isError()).isFalse();
        assertThat(response.get().getId()).isEqualTo("photo-123");
        assertThat(response.get().getTitle()).isEqualTo("Photo Title");
    }

    @Test
    void flickrException_isMappedToErrorResponse(@TempDir Path tempDir) throws Exception {
        Path photoPath = tempDir.resolve("photo.jpg");
        Files.write(photoPath, new byte[]{1});
        when(uploader.upload(any(byte[].class), any(UploadMetaData.class)))
                .thenThrow(new FlickrException("99", "Insufficient permissions"));

        PhotoServiceResponse<String> response = flickrPhotoService.uploadPhoto(new FlickrServiceRequest<>(new PhotoMetaDataV1(photoPath)));

        assertThat(response.isError()).isTrue();
        assertThat(response.responseCode()).isEqualTo(99);
        assertThat(response.responseMessage()).isEqualTo("Insufficient permissions");
    }

    @Test
    void fileReadFailure_isMappedToInternalErrorResponse(@TempDir Path tempDir) {
        Path missingPhoto = tempDir.resolve("missing.jpg");

        PhotoServiceResponse<String> response = flickrPhotoService.uploadPhoto(new FlickrServiceRequest<>(new PhotoMetaDataV1(missingPhoto)));

        assertThat(response.isError()).isTrue();
        assertThat(response.responseCode()).isEqualTo(-1);
        assertThat(response.responseMessage()).contains("missing.jpg");
        verifyNoInteractions(uploader);
    }

    private PhotoMetaDataV1 primaryPhoto(String photoId) {
        PhotoMetaDataV1 photoMetaData = new PhotoMetaDataV1(Path.of("photo.jpg"));
        photoMetaData.setPhotoId(photoId);
        photoMetaData.setPrimary(true);
        return photoMetaData;
    }
}
