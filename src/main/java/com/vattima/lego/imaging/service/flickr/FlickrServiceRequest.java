package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.service.PhotoServiceRequest;

public class FlickrServiceRequest<T> implements PhotoServiceRequest<T> {
    private T t;

    public FlickrServiceRequest(T t) {
        this.t = t;
    }

    @Override
    public T get() {
        return t;
    }
}
