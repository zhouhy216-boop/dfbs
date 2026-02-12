package com.dfbs.app.interfaces.dicttype.dto;

import com.dfbs.app.modules.dicttype.DictItemEntity;

import java.time.Instant;

public record DictItemDto(
        Long id,
        Long typeId,
        String itemValue,
        String itemLabel,
        Integer sortOrder,
        Boolean enabled,
        String note,
        Long parentId,
        Instant createdAt,
        Instant updatedAt
) {
    public static DictItemDto from(DictItemEntity e) {
        return new DictItemDto(
                e.getId(),
                e.getTypeId(),
                e.getItemValue(),
                e.getItemLabel(),
                e.getSortOrder(),
                e.getEnabled(),
                e.getNote(),
                e.getParentId(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
