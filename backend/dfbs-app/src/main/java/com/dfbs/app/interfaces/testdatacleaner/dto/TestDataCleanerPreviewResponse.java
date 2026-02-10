package com.dfbs.app.interfaces.testdatacleaner.dto;

import java.util.List;

/** Response for POST /api/v1/admin/test-data-cleaner/preview */
public record TestDataCleanerPreviewResponse(
        List<ModuleCountItemDto> items,
        long totalCount,
        boolean requiresResetConfirm,
        List<String> requiresResetReasons,
        List<String> invalidModuleIds
) {
    public record ModuleCountItemDto(String moduleId, long count) {}
}
