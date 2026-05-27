package io.legohunter.imaging.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class HostedPhotoUploadMetadata {
    private String title;
    private String description;

    @Singular
    private List<String> tags;

    private Boolean publicFlag;
    private Boolean friendFlag;
    private Boolean familyFlag;
    private Boolean hidden;
    private String safetyLevel;

    public List<String> getTags() {
        return Optional.ofNullable(tags).orElse(Collections.emptyList());
    }
}
