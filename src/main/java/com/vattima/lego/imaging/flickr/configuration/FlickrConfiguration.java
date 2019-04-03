package com.vattima.lego.imaging.flickr.configuration;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.Transport;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.vattima.lego.imaging.flickr.configuration.FlickrProperties.Secrets;

@Configuration
public class FlickrConfiguration {
    @Bean
    public Secrets getFlickrSecrets(FlickrProperties flickrProperties, @Value("${flickr.application-name}") String name) {
        return flickrProperties.getFlickr().getApplication(name).getSecrets();
    }

    @Bean
    public Transport flickrTransport(FlickrProperties flickrProperties, Secrets flickerSecrets) {
        Transport transport = new REST();
        new Flickr(flickerSecrets.getKey(), flickerSecrets.getSecret(), transport);
        RequestContext requestContext = RequestContext.getRequestContext();
        Auth auth = new Auth();
        auth.setPermission(Permission.DELETE);
        auth.setToken(flickerSecrets.getToken());
        auth.setTokenSecret(flickerSecrets.getTokenSecret());
        requestContext.setAuth(auth);
        Flickr.debugRequest = flickrProperties.getDebugRequest();
        Flickr.debugStream = flickrProperties.getDebugStream();
        return transport;
    }

    @Bean
    public PhotosetsInterface photosetsInterface(Secrets flickrSecrets, Transport flickrTransport) {
        return new PhotosetsInterface(flickrSecrets.getKey(), flickrSecrets.getSecret(), flickrTransport);
    }
}
