package io.legohunter.imaging.service;

import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photosets.Photoset;
import io.legohunter.imaging.model.AlbumManifest;

public interface FlickrPhotoService {
    PhotoServiceResponse<Photoset> createAlbum(PhotoServiceRequest<AlbumManifest> request);

    PhotoServiceResponse<Photo> getPhoto(PhotoServiceRequest<String> request);
}
