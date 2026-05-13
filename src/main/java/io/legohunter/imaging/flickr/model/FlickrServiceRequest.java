package io.legohunter.imaging.flickr.model;

import io.legohunter.imaging.model.PhotoServiceRequest;

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
