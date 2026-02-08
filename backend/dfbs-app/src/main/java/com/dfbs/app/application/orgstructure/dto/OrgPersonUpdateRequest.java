package com.dfbs.app.application.orgstructure.dto;

import java.util.List;

public record OrgPersonUpdateRequest(
        String name,
        String phone,
        String email,
        String remark,
        Long jobLevelId,
        Long primaryOrgNodeId,
        List<Long> secondaryOrgNodeIds
) {}
