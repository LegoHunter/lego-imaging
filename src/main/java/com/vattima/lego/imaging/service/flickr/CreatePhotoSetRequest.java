package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.model.AlbumManifest;

public class CreatePhotoSetRequest extends FlickrServiceRequest<AlbumManifest> {
    public CreatePhotoSetRequest(AlbumManifest albumManifest) {
        super(albumManifest);
    }
}
