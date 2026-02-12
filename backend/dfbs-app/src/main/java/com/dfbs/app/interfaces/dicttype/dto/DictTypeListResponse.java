package com.dfbs.app.interfaces.dicttype.dto;

import java.util.List;

public record DictTypeListResponse(List<DictTypeItemDto> items, long total) {}
