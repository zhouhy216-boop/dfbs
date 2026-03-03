package com.dfbs.app.interfaces.dicttype.dto;

/** typeCode/stableKey not accepted; immutable after create. */
public record UpdateDictTypeRequest(String typeName, String description, String type, Boolean enabled) {}
