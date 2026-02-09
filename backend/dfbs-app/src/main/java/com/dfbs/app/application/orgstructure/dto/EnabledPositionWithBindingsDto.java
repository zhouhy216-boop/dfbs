package com.dfbs.app.application.orgstructure.dto;

import java.util.List;

/** One enabled position in an org with its bound people. */
public record EnabledPositionWithBindingsDto(
        Long positionId,
        String baseName,
        String grade,
        String displayName,
        String shortName,
        List<PositionBoundPersonDto> boundPeople
) {}
