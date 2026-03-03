package com.dfbs.app.interfaces.dicttype.dto;

/** typeCode = stableKey (unique, immutable after create). type = A|B|C|D. */
public record CreateDictTypeRequest(String typeCode, String typeName, String description, String type, Boolean enabled) {}
