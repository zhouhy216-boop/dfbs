package com.dfbs.app.interfaces.dicttype.dto;

import java.util.List;

/** Public read response: allowed transitions for a dict type (Type B). */
public record TransitionsReadResponse(String typeCode, List<TransitionEdgeReadDto> transitions) {}
