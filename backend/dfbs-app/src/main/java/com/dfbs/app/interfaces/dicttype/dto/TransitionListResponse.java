package com.dfbs.app.interfaces.dicttype.dto;

import java.util.List;

/** List of transition edges for a dict type. */
public record TransitionListResponse(List<TransitionEdgeDto> transitions) {}
