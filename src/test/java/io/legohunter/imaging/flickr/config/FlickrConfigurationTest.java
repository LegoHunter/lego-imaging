package io.legohunter.imaging.flickr.config;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.Transport;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.Permission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlickrConfigurationTest {
    private final FlickrConfiguration configuration = new FlickrConfiguration();

    @AfterEach
    void tearDown() {
        RequestContext.getRequestContext().setAuth(null);
        Flickr.debugRequest = false;
        Flickr.debugStream = false;
    }

    @Test
    void flickrAuth_buildsDeletePermissionAuthFromSecrets() {
        FlickrProperties.Secrets secrets = new FlickrProperties.Secrets();
        secrets.setToken("oauth-token");
        secrets.setTokenSecret("oauth-token-secret");

        Auth auth = configuration.flickrAuth(secrets);

        assertThat(auth.getPermission()).isEqualTo(Permission.DELETE);
        assertThat(auth.getToken()).isEqualTo("oauth-token");
        assertThat(auth.getTokenSecret()).isEqualTo("oauth-token-secret");
    }

    @Test
    void flickrTransport_doesNotBindAuthToTheBeanCreationThread() {
        FlickrProperties properties = new FlickrProperties();
        properties.setDebugRequest(true);
        properties.setDebugStream(false);
        Auth existingAuth = new Auth();
        RequestContext.getRequestContext().setAuth(existingAuth);

        Transport transport = configuration.flickrTransport(properties);

        assertThat(transport).isInstanceOf(REST.class);
        assertThat(Flickr.debugRequest).isTrue();
        assertThat(Flickr.debugStream).isFalse();
        assertThat(RequestContext.getRequestContext().getAuth()).isSameAs(existingAuth);
    }

    @Test
    void flickrTransport_treatsMissingDebugFlagsAsDisabled() {
        FlickrProperties properties = new FlickrProperties();

        configuration.flickrTransport(properties);

        assertThat(Flickr.debugRequest).isFalse();
        assertThat(Flickr.debugStream).isFalse();
    }
}
