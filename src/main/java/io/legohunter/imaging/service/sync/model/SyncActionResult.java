package io.legohunter.imaging.service.sync.model;

import io.legohunter.imaging.model.PhotoServiceErrorType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@Builder
public class SyncActionResult {
    private String actionId;
    private SyncActionType type;
    private SyncActionStatus status;
    private String albumId;
    private String photoId;
    private Integer responseCode;
    private String message;
    private PhotoServiceErrorType errorType;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public SyncActionStatus getStatus() {
        return Optional.ofNullable(status).orElse(SyncActionStatus.PLANNED);
    }

    public boolean isFailure() {
        return getStatus() == SyncActionStatus.FAILED || getStatus() == SyncActionStatus.BLOCKED;
    }

    public PhotoServiceErrorType getErrorType() {
        if (!isFailure()) {
            return null;
        }
        return Optional.ofNullable(errorType).orElse(PhotoServiceErrorType.UNKNOWN);
    }
}
