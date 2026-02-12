package com.dfbs.app.interfaces.dicttype.dto;

import java.util.List;

public record DictItemListResponse(List<DictItemDto> items, long total) {}
