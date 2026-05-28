package io.legohunter.imaging.flickr.model;

import io.legohunter.imaging.model.HostedAlbumCreateRequest;

public class CreatePhotoSetRequest extends FlickrServiceRequest<HostedAlbumCreateRequest> {
    public CreatePhotoSetRequest(HostedAlbumCreateRequest request) {
        super(request);
    }
}
