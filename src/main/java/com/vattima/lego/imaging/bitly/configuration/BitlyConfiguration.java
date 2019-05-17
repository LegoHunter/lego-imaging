package com.vattima.lego.imaging.bitly.configuration;

import net.swisstech.bitly.BitlyClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BitlyConfiguration {
    @Bean
    public BitlyClient bitlyClient(BitlyProperties bitlyProperties) {
        return new BitlyClient(bitlyProperties.getBitly().getAccessToken());
    }
}
