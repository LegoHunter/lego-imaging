package com.vattima.lego.imaging.service.bitly;

import com.vattima.lego.imaging.api.bitly.BitlinksAPI;
import com.vattima.lego.imaging.model.bitly.Bitlink;
import com.vattima.lego.imaging.model.bitly.ShortenRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BitlinksService {
    private final BitlinksAPI bitlinksAPI;

    public Bitlink shorten(ShortenRequest request) {
        return bitlinksAPI.shorten(request);
    }
}
