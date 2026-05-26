package io.legohunter.imaging.service.sync.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
public class SyncAction {
    private String actionId;
    private SyncActionType type;
    private SyncActionSafety safety;
    private String albumId;
    private String photoId;
    private String filename;
    private String manifestPath;
    private String description;

    @Singular
    private Map<String, String> attributes;

    public SyncActionSafety getSafety() {
        return Optional.ofNullable(safety).orElse(SyncActionSafety.SAFE_AUTOMATIC);
    }

    public Map<String, String> getAttributes() {
        return Optional.ofNullable(attributes).orElse(Collections.emptyMap());
    }

    public boolean isBlocked() {
        return getSafety() == SyncActionSafety.BLOCKED;
    }

    public boolean requiresReview() {
        return getSafety() == SyncActionSafety.REQUIRES_REVIEW;
    }
}
