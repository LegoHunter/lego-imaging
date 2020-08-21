package com.vattima.lego.imaging.model.bitly;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
public class Bitlink {
    private Map<String, String> references;
    private Boolean archived;
    private List<String> tags;

    @JsonProperty("created_at")
    private String createdAt;
    private String title;
    private List<DeepLink> deeplinks;

    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("long_url")
    private String longUrl;
    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("custom_bitlinks")
    private List<String> customBitlinks;

    private String link;
    private String id;
}
