package com.vattima.lego.imaging;

import com.vattima.lego.imaging.flickr.configuration.FlickrProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableAutoConfiguration
@EnableConfigurationProperties(value = { FlickrProperties.class })
public class TestApplication {
}
