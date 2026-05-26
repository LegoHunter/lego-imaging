package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostedAlbumMetadataUpdate {
    private String albumId;
    private String title;
    private String description;
}
