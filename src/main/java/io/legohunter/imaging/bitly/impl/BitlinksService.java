package io.legohunter.imaging.bitly.impl;

import io.legohunter.imaging.bitly.api.BitlinksAPI;
import io.legohunter.imaging.bitly.model.bitly.Bitlink;
import io.legohunter.imaging.bitly.model.bitly.ShortenRequest;
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
