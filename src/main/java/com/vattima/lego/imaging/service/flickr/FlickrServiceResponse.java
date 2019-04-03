package com.vattima.lego.imaging.service.flickr;

import com.vattima.lego.imaging.service.PhotoServiceResponse;

import java.util.Optional;

public class FlickrServiceResponse<T> implements PhotoServiceResponse<T> {
    private T t;
    private Exception e;
    private String errorCode;
    private String errorMessage;



    public FlickrServiceResponse(T t) {
        this.t = t;
    }

    public FlickrServiceResponse(Exception e, String errorCode, String errorMessage) {
        this.e = e;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean isError() {
        return (e != null);
    }

    @Override
    public Integer responseCode() {
        return Optional.ofNullable(getErrorCode()).map(Integer::valueOf).orElse(0);
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String responseMessage() {
        return errorMessage;
    }

    public Exception getE() {
        return e;
    }

    @Override
    public void accept(T t) {
        this.t = t;
    }

    @Override
    public T get() {
        return t;
    }
}
