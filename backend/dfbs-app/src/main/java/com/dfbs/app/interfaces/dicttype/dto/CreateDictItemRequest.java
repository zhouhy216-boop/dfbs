package com.dfbs.app.interfaces.dicttype.dto;

public record CreateDictItemRequest(
        String itemValue,
        String itemLabel,
        Integer sortOrder,
        Boolean enabled,
        String note,
        Long parentId
) {}
