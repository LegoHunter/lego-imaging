package io.legohunter.imaging.service.sync.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyncPlanTest {
    @Test
    void builder_defaultsToDryRunWithNoActions() {
        SyncPlan plan = SyncPlan.builder().planId("plan-1").build();

        assertThat(plan.getMode()).isEqualTo(SyncPlanMode.DRY_RUN);
        assertThat(plan.isDryRun()).isTrue();
        assertThat(plan.hasActions()).isFalse();
        assertThat(plan.getActions()).isEmpty();
        assertThat(plan.getCreatedAt()).isNotNull();
    }

    @Test
    void actionCountsTrackBlockedAndReviewRequiredActions() {
        SyncPlan plan = SyncPlan.builder()
                .planId("plan-1")
                .mode(SyncPlanMode.APPLY)
                .action(SyncAction.builder()
                        .actionId("upload-1")
                        .type(SyncActionType.UPLOAD_PHOTO)
                        .build())
                .action(SyncAction.builder()
                        .actionId("membership-1")
                        .type(SyncActionType.UPDATE_ALBUM_MEMBERSHIP)
                        .safety(SyncActionSafety.REQUIRES_REVIEW)
                        .build())
                .action(SyncAction.builder()
                        .actionId("delete-1")
                        .type(SyncActionType.DELETE_PHOTO)
                        .safety(SyncActionSafety.BLOCKED)
                        .build())
                .build();

        assertThat(plan.getMode()).isEqualTo(SyncPlanMode.APPLY);
        assertThat(plan.isDryRun()).isFalse();
        assertThat(plan.hasActions()).isTrue();
        assertThat(plan.getReviewRequiredActionCount()).isEqualTo(1);
        assertThat(plan.getBlockedActionCount()).isEqualTo(1);
    }

    @Test
    void actionDefaultsToSafeAutomaticWithEmptyAttributes() {
        SyncAction action = SyncAction.builder()
                .actionId("action-1")
                .type(SyncActionType.UPDATE_PHOTO_METADATA)
                .build();

        assertThat(action.getSafety()).isEqualTo(SyncActionSafety.SAFE_AUTOMATIC);
        assertThat(action.requiresReview()).isFalse();
        assertThat(action.isBlocked()).isFalse();
        assertThat(action.getAttributes()).isEmpty();
    }
}
