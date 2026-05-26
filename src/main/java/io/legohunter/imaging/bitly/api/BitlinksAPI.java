package io.legohunter.imaging.bitly.api;

import io.legohunter.imaging.bitly.model.bitly.ShortenRequest;
import io.legohunter.imaging.bitly.model.bitly.Bitlink;
import io.legohunter.imaging.bitly.model.bitly.BitlinksPage;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

import java.util.Map;

public interface BitlinksAPI {
    @RequestLine("POST /shorten")
    @Headers("Content-Type: application/json")
    public Bitlink shorten(ShortenRequest request);

    @RequestLine("GET /groups/{groupGuid}/bitlinks")
    public BitlinksPage listBitlinks(@Param("groupGuid") String groupGuid, @QueryMap Map<String, Object> query);
}
