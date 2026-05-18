package io.legohunter.imaging.flickr.api;

import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photosets.Photoset;
import io.legohunter.imaging.model.AlbumManifest;
import io.legohunter.imaging.model.PhotoServiceRequest;
import io.legohunter.imaging.model.PhotoServiceResponse;

public interface FlickrPhotoService {
    PhotoServiceResponse<Photoset> createAlbum(PhotoServiceRequest<AlbumManifest> request);

    PhotoServiceResponse<Photo> getPhoto(PhotoServiceRequest<String> request);
}
