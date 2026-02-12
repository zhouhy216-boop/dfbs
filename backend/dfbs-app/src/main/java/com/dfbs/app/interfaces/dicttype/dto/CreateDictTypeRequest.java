package com.dfbs.app.interfaces.dicttype.dto;

public record CreateDictTypeRequest(String typeCode, String typeName, String description, Boolean enabled) {}
