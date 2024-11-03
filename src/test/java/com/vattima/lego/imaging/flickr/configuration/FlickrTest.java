package com.vattima.lego.imaging.flickr.configuration;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.Transport;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

@Slf4j
class FlickrTest {

    @Test
    @Disabled
    void flickr_photosets_getList() throws Exception {
        FlickrProperties flickrProperties = new FlickrProperties();
        flickrProperties.setClientConfigDir(Path.of("C:\\Users\\tvatt\\.credentials\\flickr.api"));
        flickrProperties.setClientConfigFile(Path.of("flickr-client-api-keys.json"));
        FlickrProperties.Secrets secrets = flickrProperties.getFlickr().getApplication("lego-imaging").getSecrets();

        Transport transport = new REST();
        new Flickr(secrets.getKey(), secrets.getSecret(), transport);
        RequestContext requestContext = RequestContext.getRequestContext();
        Auth auth = new Auth();
        auth.setPermission(Permission.READ);
        auth.setToken(secrets.getToken());
        auth.setTokenSecret(secrets.getTokenSecret());
        requestContext.setAuth(auth);
        Flickr.debugRequest = true;
        Flickr.debugStream = true;

        PhotosetsInterface photosetsInterface = new PhotosetsInterface(secrets.getKey(), secrets.getSecret(), transport);
        Photosets photosets = photosetsInterface.getList(flickrProperties.getUserId());
        log.info("photosets=[{}]", photosets);
    }
}
