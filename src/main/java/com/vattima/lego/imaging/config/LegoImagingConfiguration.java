package com.vattima.lego.imaging.config;

import net.bricklink.data.lego.ibatis.configuration.MybatisConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(LegoImagingProperties.class)
@Import(MybatisConfiguration.class)
public class LegoImagingConfiguration {
}
