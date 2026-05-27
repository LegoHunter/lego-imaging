package io.legohunter.imaging.model;

public enum PhotoServiceErrorType {
    ALBUM_NOT_FOUND(false),
    PHOTO_NOT_FOUND(false),
    PRIMARY_PHOTO_NOT_FOUND(false),
    AUTHENTICATION_FAILED(false),
    AUTHORIZATION_FAILED(false),
    INVALID_API_KEY(false),
    SERVICE_UNAVAILABLE(true),
    RATE_LIMITED(true),
    NETWORK_ERROR(true),
    WRITE_FAILED(true),
    VALIDATION_FAILED(false),
    EMPTY_PHOTO_LIST(false),
    DUPLICATE_UPLOAD(false),
    UPLOAD_LIMIT_EXCEEDED(false),
    FILE_TOO_LARGE(false),
    UNKNOWN(false);

    private final boolean retryable;

    PhotoServiceErrorType(boolean retryable) {
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
