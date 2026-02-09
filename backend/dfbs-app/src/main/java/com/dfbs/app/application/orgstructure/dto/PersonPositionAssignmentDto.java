package com.dfbs.app.application.orgstructure.dto;

/** One position assignment for a person (GET people/{id}/positions). */
public record PersonPositionAssignmentDto(
        Long orgNodeId,
        String orgNodeNamePath,
        Long positionId,
        String positionDisplayName,
        String positionShortName,
        Boolean isPartTime
) {}
