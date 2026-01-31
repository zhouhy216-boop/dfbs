package com.dfbs.app.application.repair;

import java.util.List;

/**
 * Result of repair record Excel import.
 */
public record ImportResult(
        int successCount,
        int failureCount,
        List<String> errorMessages
) {
    public static ImportResult of(int successCount, int failureCount, List<String> errors) {
        return new ImportResult(successCount, failureCount, errors != null ? errors : List.of());
    }
}
