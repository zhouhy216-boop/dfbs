package com.dfbs.app.interfaces.bizperm;

import java.util.List;

/**
 * DTOs for catalog import preview/apply. CN-only messages.
 */
public final class BizPermCatalogImportDto {

    private BizPermCatalogImportDto() {}

    /** One validation error: sheet + row (1-based) + column name + CN message. */
    public record ValidationError(String sheet, int row, String col, String message, String machineCode) {
        public ValidationError(String sheet, int row, String col, String message) {
            this(sheet, row, col, message, null);
        }
    }

    /** Preview summary: row counts and error count. */
    public record PreviewSummary(int nodeRows, int opRows, int errorCount) {}

    /** Preview response: valid flag, summary, list of errors. */
    public record ImportPreviewResponse(boolean valid, PreviewSummary summary, List<ValidationError> errors) {}

    /** Apply summary: nodes updated, ops created, ops updated. */
    public record ApplySummary(int nodesUpdated, int opsCreated, int opsUpdated) {}

    /** Apply response after successful import. */
    public record ImportApplyResponse(ApplySummary summary) {}

    /** 400 body when apply validation fails. */
    public record ImportValidationErrorResponse(String message, String machineCode, List<ValidationError> errors) {}
}
