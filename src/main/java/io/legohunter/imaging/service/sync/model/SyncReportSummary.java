package io.legohunter.imaging.service.sync.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SyncReportSummary {
    private long planned;
    private long skipped;
    private long succeeded;
    private long failed;
    private long blocked;

    public long getTotal() {
        return planned + skipped + succeeded + failed + blocked;
    }

    public boolean hasFailures() {
        return failed > 0 || blocked > 0;
    }
}
