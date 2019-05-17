package com.vattima.lego.imaging.service.bitly;

import lombok.extern.slf4j.Slf4j;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.BitlyClientException;
import net.swisstech.bitly.builder.v3.ShortenRequest;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    public void shorten_withNon200StatusCode_throwsException() throws Exception {
        String longUrlString = "http://this.isalongurl.com/that/needs/to/be/shortened?a=123";

        BitlyClient bitlyClient = setupMock(longUrlString, 500, "Something bad happened");
        BitlyService bitlyService = new BitlyService(bitlyClient);

        assertThatThrownBy(() -> {
            bitlyService.shorten(new URL(longUrlString));
        }).hasMessage("Bitly status [500] - Exception [Something bad happened]");
    }

    @Test
    public void shorten_withMalformedUrl_throwsException() throws Exception {
        String shortUrlString = "x://bit.ly/g7J4s20Lk";
        String longUrlString = "http://this.isalongurl.com/that/needs/to/be/shortened?a=123";

        BitlyClient bitlyClient = setupMock(longUrlString, shortUrlString);
        BitlyService bitlyService = new BitlyService(bitlyClient);

        assertThatThrownBy(() -> {
            bitlyService.shorten(new URL(longUrlString));
        }).hasRootCauseInstanceOf(MalformedURLException.class)
          .hasMessageContaining("unknown protocol: x");
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
        response.status_txt = "OK";
        ShortenResponse shortenResponse = new ShortenResponse();
        shortenResponse.url = shortUrlString;
        response.data = shortenResponse;
        doReturn(response).when(shortenRequest)
                          .call();
        return bitlyClient;
    }

    private BitlyClient setupMock(final String longUrlString, final int status, final String errorMessage) throws Exception {
        BitlyClient bitlyClient = mock(BitlyClient.class);
        ShortenRequest shortenRequest = mock(ShortenRequest.class);
        doReturn(shortenRequest).when(bitlyClient)
                                .shorten();
        doReturn(shortenRequest).when(shortenRequest)
                                .setLongUrl(eq(longUrlString));
        Response<ShortenResponse> response = new Response<>();
        response.status_code = status;
        response.status_txt = errorMessage;
        response.data = null;
        doReturn(response).when(shortenRequest)
                          .call();
        return bitlyClient;
    }

}