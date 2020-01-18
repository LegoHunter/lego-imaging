package com.vattima.lego.imaging.service.flickr;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.vattima.lego.imaging.LegoImagingException;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.FlickrPhotoService;
import com.vattima.lego.imaging.service.PhotoServiceRequest;
import com.vattima.lego.imaging.service.PhotoServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlickrPhotoServiceImpl implements FlickrPhotoService {
    private final PhotosetsInterface photosetsInterface;
    private final PhotosInterface photosInterface;

    @Override
    public PhotoServiceResponse<Photoset> createAlbum(PhotoServiceRequest<AlbumManifest> request) throws LegoImagingException {
        PhotoServiceResponse<Photoset> response;
        AlbumManifest albumManifest = request.get();
        try {
            if (albumManifest.getPhotos().size() < 1) {
                throw new LegoImagingException("Cannot create a Flickr Album that has no photos");
            }
            PhotoMetaData primaryPhoto = albumManifest.getPrimaryPhoto();
            Photoset photoset = photosetsInterface.create(albumManifest.getTitle(), albumManifest.getDescription(), primaryPhoto.getPhotoId());
            response = new FlickrServiceResponse<>(photoset);
        } catch (FlickrException e) {
            response = new FlickrServiceResponse<>(e, e.getErrorCode(), e.getErrorMessage());
        }
        log.debug("Flickr Response [{}]", response);
        return response;
    }

    @Override
    public PhotoServiceResponse<Photo> getPhoto(PhotoServiceRequest<String> request) {
        PhotoServiceResponse<Photo> response;
        String photoId = request.get();
        try {
            Photo photo = photosInterface.getPhoto(photoId);
            response = new FlickrServiceResponse<>(photo);
        } catch (FlickrException e) {
            response = new FlickrServiceResponse<>(e, e.getErrorCode(), e.getErrorMessage());
        }
        log.debug("Flickr Response [{}]", response);
        return response;
    }
}
