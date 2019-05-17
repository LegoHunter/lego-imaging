package com.vattima.lego.imaging.service.bitly;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.BitlyClientException;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@RequiredArgsConstructor
@Component
public class BitlyService {
    private final BitlyClient bitlyClient;

    public URL shorten(final URL longUrl) {
        Response<ShortenResponse> response = bitlyClient.shorten()
                                                        .setLongUrl(longUrl.toExternalForm())
                                                        .call();
        return extractShortUrl(response);
    }

    private URL extractShortUrl(Response<ShortenResponse> response) {
        if (response.status_code != 200) {
            throw new BitlyClientException(String.format("Bitly status [%s] - Exception [%s]", response.status_code, response.status_txt));
        }
        try {
            return new URL(response.data.url);
        } catch (MalformedURLException e) {
            throw new BitlyClientException(e);
        }
    }
}
