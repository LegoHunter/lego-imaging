package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostedAlbumPhotoSearchRequest {
    private String albumId;
    private Integer page;
    private Integer perPage;
}
