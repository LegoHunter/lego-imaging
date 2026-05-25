package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class HostedPhotoMetadataUpdate {
    private String photoId;
    private String title;
    private String description;

    @Singular
    private List<String> tags;
}
