package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class HostedAlbumCreateRequest {
    private String title;
    private String description;
    private String primaryPhotoId;

    @Singular
    private List<String> photoIds;

    public List<String> getPhotoIds() {
        return Optional.ofNullable(photoIds).orElse(Collections.emptyList());
    }
}
