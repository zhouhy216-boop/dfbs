package com.dfbs.app.application.masterdata;

import java.util.List;

/**
 * Result of part Excel import.
 */
public record PartImportResult(int created, int updated, List<String> errors) {
}
