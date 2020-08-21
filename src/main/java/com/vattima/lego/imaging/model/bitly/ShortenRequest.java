package com.vattima.lego.imaging.model.bitly;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShortenRequest {
    @JsonProperty("group_guid")
    private String groupGuid;
    private String domain;
    @JsonProperty("long_url")
    private String longUrl;
}
