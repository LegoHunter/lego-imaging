package com.vattima.lego.imaging.flickr.configuration;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.Transport;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.collections.CollectionsInterface;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.upload.UploadInterface;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.IUploader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.vattima.lego.imaging.flickr.configuration.FlickrProperties.Secrets;

@Configuration
public class FlickrConfiguration {
    @Bean
    public Secrets getFlickrSecrets(FlickrProperties flickrProperties, @Value("${flickr.application-name}") String name) {
        return flickrProperties.getFlickr()
                               .getApplication(name)
                               .getSecrets();
    }

    @Bean
    public Flickr flickr(final Secrets flickrSecrets, final Transport flickrTransport) {
        return new Flickr(flickrSecrets.getKey(), flickrSecrets.getSecret(), flickrTransport);
    }

    @Bean
    public Transport flickrTransport(FlickrProperties flickrProperties, Secrets flickerSecrets) {
        Transport transport = new REST();
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

    @Bean
    public PhotosInterface photosInterface(Secrets flickrSecrets, Transport flickrTransport) {
        return new PhotosInterface(flickrSecrets.getKey(), flickrSecrets.getSecret(), flickrTransport);
    }

    @Bean
    public CollectionsInterface collectionsInterface(Secrets flickrSecrets, Transport flickrTransport) {
        return new CollectionsInterface(flickrSecrets.getKey(), flickrSecrets.getSecret(), flickrTransport);
    }

    @Bean
    public UploadInterface uploadInterface(Secrets flickrSecrets, Transport flickrTransport) {
        return new UploadInterface(flickrSecrets.getKey(), flickrSecrets.getSecret(), flickrTransport);
    }

    @Bean
    public IUploader uploader(final Flickr flickr) {
        return flickr.getUploader();
    }
}
