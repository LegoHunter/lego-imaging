package io.legohunter.imaging.model;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface PhotoServiceResponse<T> extends Supplier<T>, Consumer<T> {
    boolean isError();
    Integer responseCode();
    String responseMessage();

    default PhotoServiceErrorType errorType() {
        return PhotoServiceErrorType.UNKNOWN;
    }

    default boolean isRetryable() {
        return isError() && errorType().isRetryable();
    }
}
