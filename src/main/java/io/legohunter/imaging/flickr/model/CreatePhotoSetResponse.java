package io.legohunter.imaging.flickr.model;

import io.legohunter.imaging.model.HostedAlbum;

public class CreatePhotoSetResponse extends FlickrServiceResponse<HostedAlbum> {
    public CreatePhotoSetResponse(HostedAlbum album) {
        super(album);
    }

    public CreatePhotoSetResponse(Exception e, String errorCode, String errorMessage) {
        super(e, errorCode, errorMessage);
    }
}
