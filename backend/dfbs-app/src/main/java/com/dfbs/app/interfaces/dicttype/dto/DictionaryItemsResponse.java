package com.dfbs.app.interfaces.dicttype.dto;

import java.util.List;

public record DictionaryItemsResponse(String typeCode, List<DictionaryItemOptionDto> items) {}
