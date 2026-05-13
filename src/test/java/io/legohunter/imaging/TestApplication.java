package io.legohunter.imaging;

import io.legohunter.imaging.flickr.config.FlickrProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableAutoConfiguration
@EnableConfigurationProperties(value = { FlickrProperties.class })
public class TestApplication {
}
