package com.vattima.lego.imaging.test;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.Transport;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoContext;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.vattima.lego.imaging.LegoImagingException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.Set;

public class MockFlickerPhotosetsInterface extends PhotosetsInterface {
    Random r = new Random();

    public MockFlickerPhotosetsInterface(String apiKey, String sharedSecret, Transport transportAPI) {
        super(apiKey, sharedSecret, transportAPI);
    }

    private String getRandom() {
        return String.valueOf(r.nextInt(100000000 - 10000000) + 10000000);
    }

    @Override
    public void addPhoto(String photosetId, String photoId) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public Photoset create(String title, String description, String primaryPhotoId) throws FlickrException {
        Photoset photoset = new Photoset();
        try {
            photoset.setId(getRandom());
            photoset.setUrl(new URL("http://bogus.com/"+photoset.getId()).toExternalForm());
        } catch (MalformedURLException e) {
            throw new LegoImagingException(e);
        }
        return photoset;
    }

    @Override
    public void delete(String photosetId) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public void editMeta(String photosetId, String title, String description) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public void editPhotos(String photosetId, String primaryPhotoId, String[] photoIds) throws FlickrException {
        // do nothing
    }

    @Override
    public PhotoContext getContext(String photoId, String photosetId) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public Photoset getInfo(String photosetId) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public Photosets getList(String userId) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public Photosets getList(String userId, String primaryPhotoExtras) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public Photosets getList(String userId, int perPage, int page, String primaryPhotoExtras) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public int getPhotosetCount(String userId) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public PhotoList<Photo> getPhotos(String photosetId, Set<String> extras, int privacy_filter, int perPage, int page) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public PhotoList<Photo> getPhotos(String photosetId, int perPage, int page) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public void orderSets(String[] photosetIds) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public void removePhoto(String photosetId, String photoId) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public void removePhotos(String photosetId, String photoIds) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public void reorderPhotos(String photosetId, String photoIds) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }

    @Override
    public void setPrimaryPhoto(String photosetId, String photoId) throws FlickrException {
        throw new FlickrException(new UnsupportedOperationException());
    }
}
