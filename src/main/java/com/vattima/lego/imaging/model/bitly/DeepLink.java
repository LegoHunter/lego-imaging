package com.vattima.lego.imaging.model.bitly;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeepLink {
    private String bitlink;

    @JsonProperty("install_url")
    private String installUrl;

    private String created;

    @JsonProperty("app_uri_path")
    private String appUriPath;

    private String modified;

    @JsonProperty("install_type")
    private String installType;

    @JsonProperty("app_guid")
    private String appGuid;

    private String guid;

    private String os;

    @JsonProperty("brand_gui")
    private String brandGui;
}
