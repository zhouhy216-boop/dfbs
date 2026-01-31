package com.dfbs.app.application.importdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One validation failure row from import.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportFailureDto {
    /** 1-based row number in the file. */
    private int rowNum;
    /** Business key that failed (e.g. customer code or blank). */
    private String uniqueKey;
    /** Reason (e.g. "Name is required"). */
    private String reason;
}
