package com.dfbs.app.interfaces.dicttype.dto;

/**
 * Read-only option for forms; no internal ids exposed.
 */
public record DictionaryItemOptionDto(
        String value,
        String label,
        int sortOrder,
        boolean enabled,
        String parentValue,
        String note
) {}
