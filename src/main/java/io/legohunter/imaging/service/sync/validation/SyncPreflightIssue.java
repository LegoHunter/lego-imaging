package io.legohunter.imaging.service.sync.validation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SyncPreflightIssue {
    private SyncPreflightSeverity severity;
    private SyncPreflightIssueType type;
    private String message;
    private String albumId;
    private String photoId;
    private String filename;
}
