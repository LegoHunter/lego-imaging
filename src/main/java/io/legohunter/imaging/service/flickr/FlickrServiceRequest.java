package io.legohunter.imaging.service.flickr;

import io.legohunter.imaging.service.PhotoServiceRequest;

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
