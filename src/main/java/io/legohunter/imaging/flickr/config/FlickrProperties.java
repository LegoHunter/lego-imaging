package io.legohunter.imaging.flickr.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@ToString
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "flickr")
public class FlickrProperties {
    private String applicationName;
    private Boolean debugRequest;
    private Boolean debugStream;
    private String userId;
    private Secrets secrets;

    @Data
    public static class Flickr {
        private String userId;
        private Secrets secrets;
    }

    @Data
    public static class Secrets {
        private String key;
        private String secret;
        private String token;
        private String tokenSecret;
    }
}
