package io.legohunter.imaging.flickr.model;

import io.legohunter.imaging.model.AlbumManifest;

public class CreatePhotoSetRequest extends FlickrServiceRequest<AlbumManifest> {
    public CreatePhotoSetRequest(AlbumManifest albumManifest) {
        super(albumManifest);
    }
}
