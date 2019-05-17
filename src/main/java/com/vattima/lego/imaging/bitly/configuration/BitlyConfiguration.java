package com.vattima.lego.imaging.bitly.configuration;

import com.vattima.lego.imaging.service.bitly.BitlyService;
import net.swisstech.bitly.BitlyClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BitlyConfiguration {
    @Bean
    public BitlyClient bitlyClient(final BitlyProperties bitlyProperties) {
        return new BitlyClient(bitlyProperties.getBitly()
                                              .getAccessToken());
    }

    @Bean
    public BitlyService bitlyService(final BitlyClient bitlyClient) {
        return new BitlyService(bitlyClient);
    }
}
