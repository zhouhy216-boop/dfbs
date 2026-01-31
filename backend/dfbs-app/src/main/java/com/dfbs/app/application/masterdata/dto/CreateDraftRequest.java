package com.dfbs.app.application.masterdata.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateDraftRequest(Long modelId, String version, LocalDate effectiveDate, List<BomItemDto> items, String createdBy) {}
