package com.dfbs.app.interfaces.dicttype.dto;

public record UpdateDictItemRequest(
        String itemLabel,
        Integer sortOrder,
        Boolean enabled,
        String note,
        Long parentId
) {}
