package com.dfbs.app.application.masterdata.dto;

/** Response after creating a BOM draft. */
public record CreateDraftResponse(Long bomId, int pendingConflictsCount, int createdPartsCount) {}
