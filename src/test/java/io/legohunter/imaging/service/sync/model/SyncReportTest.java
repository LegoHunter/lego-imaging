package io.legohunter.imaging.service.sync.model;

import io.legohunter.imaging.model.PhotoServiceErrorType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyncReportTest {
    @Test
    void reportSummarizesActionResultsByStatus() {
        SyncReport report = SyncReport.builder()
                .reportId("report-1")
                .planId("plan-1")
                .result(result("planned", SyncActionStatus.PLANNED))
                .result(result("skipped", SyncActionStatus.SKIPPED))
                .result(result("succeeded", SyncActionStatus.SUCCEEDED))
                .result(result("failed", SyncActionStatus.FAILED))
                .result(result("blocked", SyncActionStatus.BLOCKED))
                .build();

        SyncReportSummary summary = report.getSummary();

        assertThat(report.getMode()).isEqualTo(SyncPlanMode.DRY_RUN);
        assertThat(report.hasFailures()).isTrue();
        assertThat(report.getFailures())
                .extracting(SyncActionResult::getActionId)
                .containsExactly("failed", "blocked");
        assertThat(summary.getPlanned()).isEqualTo(1);
        assertThat(summary.getSkipped()).isEqualTo(1);
        assertThat(summary.getSucceeded()).isEqualTo(1);
        assertThat(summary.getFailed()).isEqualTo(1);
        assertThat(summary.getBlocked()).isEqualTo(1);
        assertThat(summary.getTotal()).isEqualTo(5);
        assertThat(summary.hasFailures()).isTrue();
    }

    @Test
    void failedActionResultDefaultsUnknownErrorType() {
        SyncActionResult result = SyncActionResult.builder()
                .actionId("action-1")
                .type(SyncActionType.CREATE_ALBUM)
                .status(SyncActionStatus.FAILED)
                .build();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getErrorType()).isEqualTo(PhotoServiceErrorType.UNKNOWN);
    }

    @Test
    void successfulActionResultDoesNotExposeErrorType() {
        SyncActionResult result = SyncActionResult.builder()
                .actionId("action-1")
                .type(SyncActionType.UPLOAD_PHOTO)
                .status(SyncActionStatus.SUCCEEDED)
                .errorType(PhotoServiceErrorType.NETWORK_ERROR)
                .build();

        assertThat(result.isFailure()).isFalse();
        assertThat(result.getErrorType()).isNull();
    }

    @Test
    void actionResultCanExposeRetryAttempts() {
        SyncActionResult result = SyncActionResult.builder()
                .actionId("action-1")
                .type(SyncActionType.UPDATE_ALBUM_METADATA)
                .status(SyncActionStatus.FAILED)
                .errorType(PhotoServiceErrorType.SERVICE_UNAVAILABLE)
                .attempts(3)
                .retried(true)
                .retryable(true)
                .build();

        assertThat(result.getErrorType()).isEqualTo(PhotoServiceErrorType.SERVICE_UNAVAILABLE);
        assertThat(result.getAttempts()).isEqualTo(3);
        assertThat(result.isRetried()).isTrue();
        assertThat(result.isRetryable()).isTrue();
    }

    @Test
    void transientErrorTypesAreRetryable() {
        assertThat(PhotoServiceErrorType.SERVICE_UNAVAILABLE.isRetryable()).isTrue();
        assertThat(PhotoServiceErrorType.RATE_LIMITED.isRetryable()).isTrue();
        assertThat(PhotoServiceErrorType.NETWORK_ERROR.isRetryable()).isTrue();
        assertThat(PhotoServiceErrorType.WRITE_FAILED.isRetryable()).isTrue();
        assertThat(PhotoServiceErrorType.AUTHENTICATION_FAILED.isRetryable()).isFalse();
        assertThat(PhotoServiceErrorType.ALBUM_NOT_FOUND.isRetryable()).isFalse();
        assertThat(PhotoServiceErrorType.VALIDATION_FAILED.isRetryable()).isFalse();
    }

    private SyncActionResult result(String actionId, SyncActionStatus status) {
        return SyncActionResult.builder()
                .actionId(actionId)
                .type(SyncActionType.UPLOAD_PHOTO)
                .status(status)
                .build();
    }
}
