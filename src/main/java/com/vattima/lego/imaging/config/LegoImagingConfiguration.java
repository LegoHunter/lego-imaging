package com.vattima.lego.imaging.config;

import net.bricklink.data.lego.ibatis.configuration.MybatisV1Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(LegoImagingProperties.class)
@Import(MybatisV1Configuration.class)
public class LegoImagingConfiguration {
}
