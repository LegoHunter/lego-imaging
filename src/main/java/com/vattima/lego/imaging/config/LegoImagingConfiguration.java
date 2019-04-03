package com.vattima.lego.imaging.config;

import net.bricklink.data.lego.ibatis.configuration.IbatisConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(LegoImagingProperties.class)
@Import(IbatisConfiguration.class)
public class LegoImagingConfiguration {
}
