package com.vattima.lego.imaging.test;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.uploader.IUploader;
import com.flickr4java.flickr.uploader.UploadMetaData;

import java.io.File;
import java.io.InputStream;
import java.util.Random;

public class MockFlickerIUploader implements IUploader {

    Random r = new Random();

    private String getRandom() {
        return String.valueOf(r.nextInt(100000000 - 10000000) + 10000000);
    }

    @Override
    public String upload(byte[] data, UploadMetaData metaData) throws FlickrException {
        return getRandom();
    }

    @Override
    public String upload(File file, UploadMetaData metaData) throws FlickrException {
        return getRandom();
    }

    @Override
    public String upload(InputStream in, UploadMetaData metaData) throws FlickrException {
        return getRandom();
    }

    @Override
    public String replace(InputStream in, String flickrId, boolean async) throws FlickrException {
        return getRandom();
    }

    @Override
    public String replace(byte[] data, String flickrId, boolean async) throws FlickrException {
        return getRandom();
    }

    @Override
    public String replace(File file, String flickrId, boolean async) throws FlickrException {
        return getRandom();
    }
}
