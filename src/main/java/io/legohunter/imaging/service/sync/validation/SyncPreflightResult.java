package io.legohunter.imaging.service.sync.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SyncPreflightResult {
    private final List<SyncPreflightIssue> issues;

    public SyncPreflightResult(List<SyncPreflightIssue> issues) {
        this.issues = Collections.unmodifiableList(new ArrayList<>(issues));
    }

    public static SyncPreflightResult ok() {
        return new SyncPreflightResult(Collections.emptyList());
    }

    public List<SyncPreflightIssue> getIssues() {
        return issues;
    }

    public List<SyncPreflightIssue> getErrors() {
        return issues.stream()
                .filter(issue -> issue.getSeverity() == SyncPreflightSeverity.ERROR)
                .toList();
    }

    public List<SyncPreflightIssue> getWarnings() {
        return issues.stream()
                .filter(issue -> issue.getSeverity() == SyncPreflightSeverity.WARNING)
                .toList();
    }

    public boolean isOk() {
        return issues.isEmpty();
    }

    public boolean hasErrors() {
        return !getErrors().isEmpty();
    }
}
