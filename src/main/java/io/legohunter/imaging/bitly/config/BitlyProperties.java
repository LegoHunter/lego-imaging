package io.legohunter.imaging.bitly.config;

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
@ConfigurationProperties(prefix = "bitly")
public class BitlyProperties {
    private String baseUrl = "https://api-ssl.bitly.com/v4";
    private String accessToken;
    private String groupGuid;
    private OAuth2 oAuth2;

    @Data
    public static class OAuth2 {
        private String clientId;
        private String clientSecret;
    }
}
