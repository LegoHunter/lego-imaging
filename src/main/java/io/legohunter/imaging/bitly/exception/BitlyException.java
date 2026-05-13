package io.legohunter.imaging.bitly.exception;

import io.legohunter.imaging.exception.LegoImagingException;
import io.legohunter.imaging.bitly.model.bitly.BitlyError;

public class BitlyException extends LegoImagingException {
    private BitlyError bitlyError;

    public BitlyException(BitlyError bitlyError) {
        super();
        this.bitlyError = bitlyError;
    }

    public BitlyException(BitlyError bitlyError, String message) {
        super(message);
        this.bitlyError = bitlyError;
    }

    public BitlyException(BitlyError bitlyError, String message, Throwable cause) {
        super(message, cause);
        this.bitlyError = bitlyError;
    }

    public BitlyException(BitlyError bitlyError, Throwable cause) {
        super(cause);
        this.bitlyError = bitlyError;
    }

    protected BitlyException(BitlyError bitlyError, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.bitlyError = bitlyError;
    }
}
