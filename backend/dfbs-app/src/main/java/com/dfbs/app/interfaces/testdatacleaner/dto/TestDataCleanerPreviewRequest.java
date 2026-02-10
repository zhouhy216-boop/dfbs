package com.dfbs.app.interfaces.testdatacleaner.dto;

import java.util.List;

/** Request for POST /api/v1/admin/test-data-cleaner/preview. includeAttachments ignored in v0. */
public record TestDataCleanerPreviewRequest(
        List<String> moduleIds,
        Boolean includeAttachments
) {}
