package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostedAlbum {
    private String id;
    private String url;
    private String title;
    private String description;
    private String primaryPhotoId;
    private Integer photoCount;
}
