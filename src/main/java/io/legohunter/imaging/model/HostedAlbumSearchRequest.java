package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostedAlbumSearchRequest {
    private String userId;
    private Integer page;
    private Integer perPage;
}
