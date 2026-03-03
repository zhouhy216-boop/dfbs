package com.dfbs.app.interfaces.dicttype.dto;

/** One edge for upsert (item_value as stable key). */
public record TransitionEdgeRequest(String fromValue, String toValue, Boolean enabled) {}
