package com.dfbs.app.interfaces.testdatacleaner.dto;

import java.util.List;

/** Response for POST /api/v1/admin/test-data-cleaner/execute */
public record TestDataCleanerExecuteResponse(
        String startedAt,
        String finishedAt,
        boolean requiresResetConfirm,
        List<String> requiresResetReasons,
        List<String> invalidModuleIds,
        List<ModuleExecuteItemDto> items,
        long totalDeleted,
        String status,
        String redisMessage
) {
    public record ModuleExecuteItemDto(
            String moduleId,
            List<TableResultDto> tables,
            long moduleDeletedTotal,
            String moduleStatus
    ) {}

    public record TableResultDto(
            String table,
            int deleted,
            String status,
            String error
    ) {}
}
