package io.legohunter.imaging.flickr.model;

import io.legohunter.imaging.model.PhotoServiceResponse;
import io.legohunter.imaging.model.PhotoServiceErrorType;

import java.util.Optional;

public class FlickrServiceResponse<T> implements PhotoServiceResponse<T> {
    public static final String INTERNAL_ERROR_CODE = "-1";

    private T t;
    private Exception e;
    private String errorCode;
    private String errorMessage;
    private PhotoServiceErrorType errorType;



    public FlickrServiceResponse(T t) {
        this.t = t;
    }

    public FlickrServiceResponse(Exception e, String errorCode, String errorMessage) {
        this(e, errorCode, errorMessage, PhotoServiceErrorType.UNKNOWN);
    }

    public FlickrServiceResponse(Exception e, String errorCode, String errorMessage, PhotoServiceErrorType errorType) {
        this.e = e;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
    }

    public FlickrServiceResponse(Exception e) {
        this(e, INTERNAL_ERROR_CODE, e.getMessage());
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

    @Override
    public PhotoServiceErrorType errorType() {
        return Optional.ofNullable(errorType).orElse(PhotoServiceErrorType.UNKNOWN);
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
