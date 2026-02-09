package com.dfbs.app.application.orgstructure.dto;

/** Person bound to a position: contact info + primary org + isPartTime (primary org != current org). */
public record PositionBoundPersonDto(
        Long id,
        String name,
        String phone,
        String email,
        Long primaryOrgNodeId,
        String primaryOrgNamePath,
        Boolean isPartTime
) {}
