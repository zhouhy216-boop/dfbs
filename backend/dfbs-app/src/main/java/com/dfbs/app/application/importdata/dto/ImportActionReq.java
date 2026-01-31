package com.dfbs.app.application.importdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single action for conflict resolution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportActionReq {
    /** SKIP = do not apply, UPDATE = overwrite existing, REUSE = keep existing. */
    private ImportAction action;
    /** Unique identifier for the row (e.g. row number or business key). */
    private String rowKey;

    public enum ImportAction {
        SKIP,
        UPDATE,
        REUSE
    }
}
