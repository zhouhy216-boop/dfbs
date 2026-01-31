package com.dfbs.app.application.inventory;

/**
 * Result of warehouse selection validation (e.g. branch selecting HQ).
 */
public enum WarehouseSelectionResult {
    OK,              // No special reason required
    REQUIRES_REASON  // Branch has stock but selected HQ -> reason required / notify admin
}
