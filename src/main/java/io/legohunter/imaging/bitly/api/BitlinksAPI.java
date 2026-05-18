package io.legohunter.imaging.bitly.api;

import io.legohunter.imaging.bitly.model.bitly.ShortenRequest;
import io.legohunter.imaging.bitly.model.bitly.Bitlink;
import feign.Headers;
import feign.RequestLine;

public interface BitlinksAPI {
    @RequestLine("POST /shorten")
    @Headers("Content-Type: application/json")
    public Bitlink shorten(ShortenRequest request);
}
