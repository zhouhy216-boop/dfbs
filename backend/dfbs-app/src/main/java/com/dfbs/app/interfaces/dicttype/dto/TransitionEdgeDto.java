package com.dfbs.app.interfaces.dicttype.dto;

/** One transition edge for list response (fromValue/fromLabel, toValue/toLabel, enabled). */
public record TransitionEdgeDto(
        Long id,
        String fromValue,
        String fromLabel,
        String toValue,
        String toLabel,
        Boolean enabled
) {}
