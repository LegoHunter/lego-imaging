package com.vattima.lego.imaging.service.bitly;

import lombok.extern.slf4j.Slf4j;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.builder.v3.ShortenRequest;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;
import org.junit.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


@Slf4j
public class BitlyServiceTest {

    @Test
    public void shorten_withValidUrl_shortensUrl() throws Exception {
        String shortUrlString = "http://bit.ly/g7J4s20Lk";
        String longUrlString = "http://this.isalongurl.com/that/needs/to/be/shortened?a=123";

        BitlyClient bitlyClient = setupMock(longUrlString, shortUrlString);
        BitlyService bitlyService = new BitlyService(bitlyClient);
        URL shortUrl = bitlyService.shorten(new URL(longUrlString));
        log.info("URL [{}]", shortUrl);
        assertThat(shortUrl).isEqualTo(new URL(shortUrlString));
    }

    private BitlyClient setupMock(final String longUrlString, final String shortUrlString) throws Exception {
        BitlyClient bitlyClient = mock(BitlyClient.class);
        ShortenRequest shortenRequest = mock(ShortenRequest.class);
        doReturn(shortenRequest).when(bitlyClient)
                                .shorten();
        doReturn(shortenRequest).when(shortenRequest)
                                .setLongUrl(eq(longUrlString));
        Response<ShortenResponse> response = new Response<>();
        response.status_code = 200;
        ShortenResponse shortenResponse = new ShortenResponse();
        shortenResponse.url = new URL(shortUrlString).toExternalForm();
        response.data = shortenResponse;
        doReturn(response).when(shortenRequest)
                          .call();
        return bitlyClient;
    }

}