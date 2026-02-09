package com.dfbs.app.application.orgstructure.dto;

import java.util.List;

/** GET by-org response: list of enabled positions with bound people. */
public record PositionsByOrgResponseDto(
        Long orgNodeId,
        String orgNodeName,
        List<EnabledPositionWithBindingsDto> enabledPositions
) {}
