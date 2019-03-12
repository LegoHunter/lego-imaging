package com.vattima.lego.imaging.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ToString
@ConfigurationProperties(prefix = "lego.imaging")
public class LegoImagingProperties {
    private String rootImagesFolder;
    private String rootInventoryItemsFolder;
    private String keywordsKeyName;
}
