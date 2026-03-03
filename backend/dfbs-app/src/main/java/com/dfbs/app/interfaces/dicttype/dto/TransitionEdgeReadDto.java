package com.dfbs.app.interfaces.dicttype.dto;

/** One transition edge for public read API (no internal id). */
public record TransitionEdgeReadDto(
        String fromValue,
        String toValue,
        Boolean enabled,
        String fromLabel,
        String toLabel
) {}
