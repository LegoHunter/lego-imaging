package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostedPhoto {
    private String id;
    private String title;
    private String description;
    private String url;
    private Boolean primary;
}
