package com.dfbs.app.application.smartselect.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TempPoolConfirmRequest {
    private Long id;
    private String entityType;
    /** "RENAME" (default): set is_temp=false and apply finalValues. "MERGE": re-link FKs to targetId then delete temp. */
    private String mode;
    /** When mode=MERGE: the real master record ID to merge into (all references to id will point here). */
    private Long targetId;
    private Map<String, Object> finalValues; // name, customerCode, modelId, etc.
}
