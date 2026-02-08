package com.dfbs.app.application.orgstructure.dto;

import java.time.Instant;
import java.util.List;

/** Person with primaryOrgNodeId, secondaryOrgNodeIds, optional jobLevelDisplayName for cards. */
public record OrgPersonResponse(
        Long id,
        String name,
        String phone,
        String email,
        String remark,
        Long jobLevelId,
        Boolean isActive,
        Long primaryOrgNodeId,
        List<Long> secondaryOrgNodeIds,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy,
        String jobLevelDisplayName
) {
    public OrgPersonResponse(Long id, String name, String phone, String email, String remark,
                             Long jobLevelId, Boolean isActive, Long primaryOrgNodeId, List<Long> secondaryOrgNodeIds,
                             Instant createdAt, String createdBy, Instant updatedAt, String updatedBy) {
        this(id, name, phone, email, remark, jobLevelId, isActive, primaryOrgNodeId, secondaryOrgNodeIds,
                createdAt, createdBy, updatedAt, updatedBy, null);
    }
}
