package com.dfbs.app.interfaces.dicttype.dto;

import com.dfbs.app.modules.dicttype.DictTypeEntity;

import java.time.Instant;

public record DictTypeItemDto(
        Long id,
        String typeCode,
        String typeName,
        String description,
        String type,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public static DictTypeItemDto from(DictTypeEntity e) {
        return new DictTypeItemDto(
                e.getId(),
                e.getTypeCode(),
                e.getTypeName(),
                e.getDescription(),
                e.getType() != null ? e.getType() : "A",
                e.getEnabled(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
