package com.dfbs.app.interfaces.dicttype.dto;

/**
 * Read-only type option; no internal id exposed.
 */
public record DictionaryTypeOptionDto(String typeCode, String typeName, boolean enabled) {}
