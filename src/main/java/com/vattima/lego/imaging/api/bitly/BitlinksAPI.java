package com.vattima.lego.imaging.api.bitly;

import com.vattima.lego.imaging.model.bitly.ShortenRequest;
import com.vattima.lego.imaging.model.bitly.Bitlink;
import feign.Headers;
import feign.RequestLine;

public interface BitlinksAPI {
    @RequestLine("POST /shorten")
    @Headers("Content-Type: application/json")
    public Bitlink shorten(ShortenRequest request);
}
