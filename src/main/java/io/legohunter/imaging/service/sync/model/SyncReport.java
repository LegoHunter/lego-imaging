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
public class SyncReport {
    private String reportId;
    private String planId;

    @Builder.Default
    private SyncPlanMode mode = SyncPlanMode.DRY_RUN;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @Singular
    private List<SyncActionResult> results;

    public SyncPlanMode getMode() {
        return Optional.ofNullable(mode).orElse(SyncPlanMode.DRY_RUN);
    }

    public List<SyncActionResult> getResults() {
        return Optional.ofNullable(results).orElse(Collections.emptyList());
    }

    public boolean hasFailures() {
        return getResults().stream().anyMatch(SyncActionResult::isFailure);
    }

    public List<SyncActionResult> getFailures() {
        return getResults().stream()
                .filter(SyncActionResult::isFailure)
                .toList();
    }

    public SyncReportSummary getSummary() {
        return SyncReportSummary.builder()
                .planned(count(SyncActionStatus.PLANNED))
                .skipped(count(SyncActionStatus.SKIPPED))
                .succeeded(count(SyncActionStatus.SUCCEEDED))
                .failed(count(SyncActionStatus.FAILED))
                .blocked(count(SyncActionStatus.BLOCKED))
                .build();
    }

    private long count(SyncActionStatus status) {
        return getResults().stream()
                .filter(result -> result.getStatus() == status)
                .count();
    }
}
