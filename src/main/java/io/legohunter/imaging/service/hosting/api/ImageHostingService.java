package io.legohunter.imaging.service.hosting.api;

import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.HostedAlbum;
import io.legohunter.imaging.model.HostedAlbumMembershipRequest;
import io.legohunter.imaging.model.HostedAlbumMetadataUpdate;
import io.legohunter.imaging.model.HostedPhoto;
import io.legohunter.imaging.model.HostedPhotoMetadataUpdate;
import io.legohunter.imaging.model.PhotoMetaDataV1;
import io.legohunter.imaging.model.PhotoServiceRequest;
import io.legohunter.imaging.model.PhotoServiceResponse;

public interface ImageHostingService {
    PhotoServiceResponse<String> uploadPhoto(PhotoServiceRequest<PhotoMetaDataV1> request);

    PhotoServiceResponse<String> replacePhoto(PhotoServiceRequest<PhotoMetaDataV1> request);

    PhotoServiceResponse<Void> deletePhoto(PhotoServiceRequest<String> request);

    PhotoServiceResponse<HostedAlbum> createAlbum(PhotoServiceRequest<AlbumManifest> request);

    PhotoServiceResponse<Void> updateAlbumMembership(PhotoServiceRequest<HostedAlbumMembershipRequest> request);

    PhotoServiceResponse<Void> updateAlbumMetadata(PhotoServiceRequest<HostedAlbumMetadataUpdate> request);

    PhotoServiceResponse<Void> updatePhotoMetadata(PhotoServiceRequest<HostedPhotoMetadataUpdate> request);

    PhotoServiceResponse<HostedPhoto> getPhoto(PhotoServiceRequest<String> request);
}
