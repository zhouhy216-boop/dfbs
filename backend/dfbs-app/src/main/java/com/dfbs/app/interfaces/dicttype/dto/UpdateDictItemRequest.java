package com.dfbs.app.interfaces.dicttype.dto;

/** item_value (itemKey) is immutable and not accepted in update. */
public record UpdateDictItemRequest(
        String itemLabel,
        Integer sortOrder,
        Boolean enabled,
        String note,
        Long parentId
) {}
