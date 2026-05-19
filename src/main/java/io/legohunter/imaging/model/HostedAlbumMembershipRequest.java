package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class HostedAlbumMembershipRequest {
    private String albumId;
    private String primaryPhotoId;
    private Set<String> photoIds;
}
