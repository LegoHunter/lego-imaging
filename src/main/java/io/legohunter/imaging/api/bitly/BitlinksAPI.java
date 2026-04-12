package io.legohunter.imaging.api.bitly;

import io.legohunter.imaging.model.bitly.ShortenRequest;
import io.legohunter.imaging.model.bitly.Bitlink;
import feign.Headers;
import feign.RequestLine;

public interface BitlinksAPI {
    @RequestLine("POST /shorten")
    @Headers("Content-Type: application/json")
    public Bitlink shorten(ShortenRequest request);
}
