package com.dfbs.app.application.importdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of an import run: counts and lists of failures/conflicts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDto {
    private int successCount;
    private int failureCount;
    private int conflictCount;

    @Builder.Default
    private List<ImportFailureDto> failures = new ArrayList<>();

    @Builder.Default
    private List<ImportConflictDto> conflicts = new ArrayList<>();
}
