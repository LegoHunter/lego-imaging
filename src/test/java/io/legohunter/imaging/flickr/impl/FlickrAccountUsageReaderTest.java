package io.legohunter.imaging.flickr.impl;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.Response;
import com.flickr4java.flickr.Transport;
import io.legohunter.imaging.flickr.config.FlickrProperties;
import io.legohunter.imaging.model.HostedAccountUsage;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlickrAccountUsageReaderTest {

    @Test
    void aFreeAccountHasACountAndALimit() throws Exception {
        HostedAccountUsage usage = FlickrAccountUsageReader.parse(element(
                "<user id=\"1@N04\" ispro=\"0\" upload_count=\"6\" upload_limit=\"1000\" upload_limit_status=\"below_limit\"/>"));

        assertThat(usage.itemCount()).isEqualTo(6);
        assertThat(usage.itemLimit()).isEqualTo(1000);
        assertThat(usage.limited()).isTrue();
        assertThat(usage.remaining()).isEqualTo(994);
    }

    @Test
    void aProAccountReportsMinusOneWhichMeansNoLimit() throws Exception {
        HostedAccountUsage usage = FlickrAccountUsageReader.parse(element(
                "<user id=\"1@N08\" ispro=\"1\" upload_count=\"24703\" upload_limit=\"-1\" upload_limit_status=\"no_limit\"/>"));

        assertThat(usage.itemCount()).isEqualTo(24703);
        assertThat(usage.itemLimit()).isNull();
        assertThat(usage.limited()).isFalse();
        assertThat(usage.remaining()).isNull();
    }

    @Test
    void theValuesAreAlsoReadFromChildElements() throws Exception {
        HostedAccountUsage usage = FlickrAccountUsageReader.parse(element(
                "<user id=\"1@N04\"><username>test</username><upload_count>12</upload_count><upload_limit>1000</upload_limit></user>"));

        assertThat(usage.itemCount()).isEqualTo(12);
        assertThat(usage.itemLimit()).isEqualTo(1000);
    }

    @Test
    void anAccountThatSaysNothingIsTreatedAsUnlimitedWithAnUnknownCount() throws Exception {
        HostedAccountUsage usage = FlickrAccountUsageReader.parse(element("<user id=\"1@N04\" ispro=\"0\"/>"));

        assertThat(usage.itemCount()).isNull();
        assertThat(usage.limited()).isFalse();
    }

    @Test
    void readCallsGetUploadStatusWithTheApplicationKey() throws Exception {
        Transport transport = mock(Transport.class);
        when(transport.getPath()).thenReturn("/services/rest/");
        Response response = mock(Response.class);
        when(response.isError()).thenReturn(false);
        when(response.getPayload()).thenReturn(element("<user upload_count=\"3\" upload_limit=\"1000\"/>"));
        when(transport.get(eq("/services/rest/"), eq(Map.<String, Object>of("method", "flickr.people.getUploadStatus")),
                eq("the-key"), eq("the-secret"))).thenReturn(response);

        HostedAccountUsage usage = new FlickrAccountUsageReader(transport, secrets()).read();

        assertThat(usage).isEqualTo(new HostedAccountUsage(3, 1000));
    }

    @Test
    void readRaisesTheProvidersError() throws Exception {
        Transport transport = mock(Transport.class);
        when(transport.getPath()).thenReturn("/services/rest/");
        Response response = mock(Response.class);
        when(response.isError()).thenReturn(true);
        when(response.getErrorCode()).thenReturn("98");
        when(response.getErrorMessage()).thenReturn("Invalid auth token");
        when(transport.get(any(), any(), any(), any())).thenReturn(response);

        assertThatThrownBy(() -> new FlickrAccountUsageReader(transport, secrets()).read())
                .isInstanceOf(FlickrException.class)
                .hasMessageContaining("Invalid auth token");
    }

    private static FlickrProperties.Secrets secrets() {
        FlickrProperties.Secrets secrets = new FlickrProperties.Secrets();
        secrets.setKey("the-key");
        secrets.setSecret("the-secret");
        return secrets;
    }

    private static Element element(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)))
                .getDocumentElement();
    }
}
