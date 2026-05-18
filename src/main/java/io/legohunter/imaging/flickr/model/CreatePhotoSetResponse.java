package io.legohunter.imaging.flickr.model;

import com.flickr4java.flickr.photosets.Photoset;

public class CreatePhotoSetResponse extends FlickrServiceResponse<Photoset> {
    public CreatePhotoSetResponse(Photoset photoset) {
        super(photoset);
    }

    public CreatePhotoSetResponse(Exception e, String errorCode, String errorMessage) {
        super(e, errorCode, errorMessage);
    }
}
