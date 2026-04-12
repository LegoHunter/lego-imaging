package io.legohunter.imaging.service.bitly;

import io.legohunter.imaging.api.bitly.BitlinksAPI;
import io.legohunter.imaging.model.bitly.Bitlink;
import io.legohunter.imaging.model.bitly.ShortenRequest;
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
