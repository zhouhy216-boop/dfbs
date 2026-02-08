package com.dfbs.app.application.orgstructure.dto;

import com.dfbs.app.modules.orgstructure.OrgNodeEntity;

import java.time.Instant;

/** Flat DTO for single node or list item; no JPA relations (avoids lazy serialization). */
public record OrgNodeDto(
        Long id,
        Long levelId,
        Long parentId,
        String name,
        String remark,
        Boolean isEnabled,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
    public static OrgNodeDto from(OrgNodeEntity e) {
        return new OrgNodeDto(
                e.getId(),
                e.getLevelId(),
                e.getParentId(),
                e.getName(),
                e.getRemark(),
                e.getIsEnabled(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getCreatedBy(),
                e.getUpdatedBy()
        );
    }
}
