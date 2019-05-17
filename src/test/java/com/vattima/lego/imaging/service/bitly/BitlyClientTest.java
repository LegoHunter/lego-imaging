package com.vattima.lego.imaging.service.bitly;

import com.vattima.lego.imaging.bitly.configuration.BitlyConfiguration;
import com.vattima.lego.imaging.bitly.configuration.BitlyProperties;
import lombok.extern.slf4j.Slf4j;
import net.swisstech.bitly.BitlyClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BitlyConfiguration.class, BitlyProperties.class})
@EnableConfigurationProperties
@ActiveProfiles("bitly-client-test")
public class BitlyClientTest {
    @Autowired
    private BitlyClient bitlyClient;

    @Test
    public void bitlyClient_isAutowired() {
        assertThat(bitlyClient).isNotNull();
        log.info("{[]}", bitlyClient);
    }
}
