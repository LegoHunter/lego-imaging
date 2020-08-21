package com.vattima.lego.imaging;

import com.vattima.lego.imaging.model.bitly.BitlyError;

public class BitlyException extends RuntimeException {
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
