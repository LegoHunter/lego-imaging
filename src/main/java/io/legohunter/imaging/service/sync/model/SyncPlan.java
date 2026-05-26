package io.legohunter.imaging.service.sync.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class SyncPlan {
    private String planId;

    @Builder.Default
    private SyncPlanMode mode = SyncPlanMode.DRY_RUN;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Singular
    private List<SyncAction> actions;

    public SyncPlanMode getMode() {
        return Optional.ofNullable(mode).orElse(SyncPlanMode.DRY_RUN);
    }

    public List<SyncAction> getActions() {
        return Optional.ofNullable(actions).orElse(Collections.emptyList());
    }

    public boolean isDryRun() {
        return getMode() == SyncPlanMode.DRY_RUN;
    }

    public boolean hasActions() {
        return !getActions().isEmpty();
    }

    public long getBlockedActionCount() {
        return getActions().stream()
                .filter(SyncAction::isBlocked)
                .count();
    }

    public long getReviewRequiredActionCount() {
        return getActions().stream()
                .filter(SyncAction::requiresReview)
                .count();
    }
}
