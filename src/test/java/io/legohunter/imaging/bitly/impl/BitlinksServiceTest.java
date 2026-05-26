package io.legohunter.imaging.bitly.impl;

import io.legohunter.imaging.bitly.api.BitlinksAPI;
import io.legohunter.imaging.bitly.model.bitly.Bitlink;
import io.legohunter.imaging.bitly.model.bitly.BitlinksPage;
import io.legohunter.imaging.bitly.model.bitly.ShortenRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BitlinksServiceTest {
    @Mock
    private BitlinksAPI bitlinksAPI;

    @Test
    void shortenDelegatesToBitlinksApi() {
        BitlinksService service = new BitlinksService(bitlinksAPI);
        ShortenRequest request = new ShortenRequest();
        Bitlink bitlink = new Bitlink();
        when(bitlinksAPI.shorten(request)).thenReturn(bitlink);

        Bitlink response = service.shorten(request);

        assertThat(response).isSameAs(bitlink);
        verify(bitlinksAPI).shorten(request);
    }

    @Test
    void listBitlinksBuildsQueryFromProvidedValues() {
        BitlinksService service = new BitlinksService(bitlinksAPI);
        BitlinksPage page = new BitlinksPage();
        when(bitlinksAPI.listBitlinks(org.mockito.ArgumentMatchers.eq("group-1"), org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(page);

        BitlinksPage response = service.listBitlinks("group-1", 100, "cursor-1", "both");

        assertThat(response).isSameAs(page);
        ArgumentCaptor<Map<String, Object>> queryCaptor = ArgumentCaptor.captor();
        verify(bitlinksAPI).listBitlinks(org.mockito.ArgumentMatchers.eq("group-1"), queryCaptor.capture());
        assertThat(queryCaptor.getValue())
                .containsEntry("size", 100)
                .containsEntry("search_after", "cursor-1")
                .containsEntry("archived", "both");
    }

    @Test
    void listBitlinksOmitsBlankOptionalValues() {
        BitlinksService service = new BitlinksService(bitlinksAPI);
        when(bitlinksAPI.listBitlinks(org.mockito.ArgumentMatchers.eq("group-1"), org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(new BitlinksPage());

        service.listBitlinks("group-1", 0, " ", null);

        ArgumentCaptor<Map<String, Object>> queryCaptor = ArgumentCaptor.captor();
        verify(bitlinksAPI).listBitlinks(org.mockito.ArgumentMatchers.eq("group-1"), queryCaptor.capture());
        assertThat(queryCaptor.getValue()).isEmpty();
    }
}
