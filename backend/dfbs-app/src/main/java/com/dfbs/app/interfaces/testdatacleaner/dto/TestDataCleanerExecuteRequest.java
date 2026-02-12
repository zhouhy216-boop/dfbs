package com.dfbs.app.interfaces.testdatacleaner.dto;

import java.util.List;

/** Request for POST /api/v1/admin/test-data-cleaner/execute */
public record TestDataCleanerExecuteRequest(
        List<String> moduleIds,
        String confirmText,
        Boolean includeAttachments
) {}
