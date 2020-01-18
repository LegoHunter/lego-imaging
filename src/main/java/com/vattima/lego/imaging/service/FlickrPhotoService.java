package com.vattima.lego.imaging.service;

import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photosets.Photoset;
import com.vattima.lego.imaging.model.AlbumManifest;

public interface FlickrPhotoService {
    PhotoServiceResponse<Photoset> createAlbum(PhotoServiceRequest<AlbumManifest> request);

    PhotoServiceResponse<Photo> getPhoto(PhotoServiceRequest<String> request);
}
