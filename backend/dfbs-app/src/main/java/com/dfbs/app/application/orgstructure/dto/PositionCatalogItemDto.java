package com.dfbs.app.application.orgstructure.dto;

/** Read-only catalog item: baseName, grade, displayName, shortName, isEnabled. */
public record PositionCatalogItemDto(
        Long id,
        String baseName,
        String grade,
        String displayName,
        String shortName,
        Boolean isEnabled
) {}
