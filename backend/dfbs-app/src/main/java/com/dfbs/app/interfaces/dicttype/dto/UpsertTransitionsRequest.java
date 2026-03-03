package com.dfbs.app.interfaces.dicttype.dto;

import java.util.List;

/** Batch upsert request for transitions. */
public record UpsertTransitionsRequest(List<TransitionEdgeRequest> transitions) {}
