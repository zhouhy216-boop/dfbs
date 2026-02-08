package com.dfbs.app.application.orgstructure.dto;

import com.dfbs.app.modules.orgstructure.OrgNodeEntity;

import java.util.ArrayList;
import java.util.List;

/** Tree node for API (id, name, levelId, parentId, isEnabled, children). */
public record OrgTreeNodeDto(
        Long id,
        String name,
        Long levelId,
        Long parentId,
        Boolean isEnabled,
        String remark,
        List<OrgTreeNodeDto> children
) {
    public static OrgTreeNodeDto from(OrgNodeEntity e, List<OrgTreeNodeDto> children) {
        return new OrgTreeNodeDto(
                e.getId(),
                e.getName(),
                e.getLevelId(),
                e.getParentId(),
                e.getIsEnabled(),
                e.getRemark(),
                children != null ? children : List.of()
        );
    }
}
