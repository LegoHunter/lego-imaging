package io.legohunter.imaging.bitly.impl;

import io.legohunter.imaging.bitly.api.BitlinksAPI;
import io.legohunter.imaging.bitly.model.bitly.Bitlink;
import io.legohunter.imaging.bitly.model.bitly.BitlinksPage;
import io.legohunter.imaging.bitly.model.bitly.ShortenRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class BitlinksService {
    private final BitlinksAPI bitlinksAPI;

    public Bitlink shorten(ShortenRequest request) {
        return bitlinksAPI.shorten(request);
    }

    public BitlinksPage listBitlinks(String groupGuid, int size, String searchAfter, String archived) {
        Map<String, Object> query = new LinkedHashMap<>();
        if (size > 0) {
            query.put("size", size);
        }
        if (searchAfter != null && !searchAfter.isBlank()) {
            query.put("search_after", searchAfter);
        }
        if (archived != null && !archived.isBlank()) {
            query.put("archived", archived);
        }
        return bitlinksAPI.listBitlinks(groupGuid, query);
    }
}
