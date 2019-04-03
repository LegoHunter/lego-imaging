package com.vattima.lego.imaging.service;

import com.flickr4java.flickr.photosets.Photoset;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.service.PhotoServiceRequest;
import com.vattima.lego.imaging.service.PhotoServiceResponse;

public interface FlickrPhotoService {
    PhotoServiceResponse<Photoset> createAlbum(PhotoServiceRequest<AlbumManifest> request);

//    <R extends PhotoServiceRequest, S extends PhotoServiceResponse> S updateAlbum(R request);
//
//    <R extends PhotoServiceRequest, S extends PhotoServiceResponse> S addPhotoToAlbum(R request);
//
//    <R extends PhotoServiceRequest, S extends PhotoServiceResponse> S uploadAllAlbumPhotos(R request);
//
//    <R extends PhotoServiceRequest, S extends PhotoServiceResponse> S photoIsUpdated(R request);
//
//    <R extends PhotoServiceRequest, S extends PhotoServiceResponse> S loadAlbumManifest(R request);
//
//    <R extends PhotoServiceRequest, S extends PhotoServiceResponse> S saveAlbumManifest(R request);
//
//    <R extends PhotoServiceRequest, S extends PhotoServiceResponse> S getAlbumPhotoMetaData(R request);

//    private final PhotosetsInterface photosetsInterface;
//    public CreatePhotoSetResponse createAlbum(FlickrServiceRequest<AlbumManifest> request) {
//        AlbumManifest albumManifest = request.get();
//        Photoset photoset = null;
//        CreatePhotoSetResponse response = null;
//        try {
//            photoset = photosetsInterface.create(albumManifest.getTitle(), albumManifest.getDescription(), "43870014095");
//            response = new CreatePhotoSetResponse(photoset);
//        } catch (FlickrException e) {
//            response = new CreatePhotoSetResponse(e, e.getErrorCode(), e.getErrorMessage());
//        }
//        return response;
//    }
}
