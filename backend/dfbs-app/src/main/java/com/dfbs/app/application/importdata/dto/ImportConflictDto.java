package com.dfbs.app.application.importdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One conflict row: existing record vs import row (do not overwrite on first pass).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportConflictDto {
    /** 1-based row number in the file. */
    private int rowNum;
    /** Business unique key (e.g. customer code). */
    private String uniqueKey;
    /** Existing record snapshot as JSON. */
    private String originalData;
    /** Import row data as JSON. */
    private String importData;
}
