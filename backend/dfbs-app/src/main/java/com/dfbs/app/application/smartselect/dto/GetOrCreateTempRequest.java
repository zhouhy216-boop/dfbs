package com.dfbs.app.application.smartselect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request for getOrCreateTemp. entityType + uniqueKey identify the record; payload has entity-specific fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOrCreateTempRequest {
    private String entityType; // CUSTOMER, MACHINE, PART, CONTRACT, SIM, MODEL
    private String uniqueKey;  // customerCode, machineNo, partNo, contractNo, cardNo, modelNo
    private Map<String, Object> payload; // name, modelId, etc.
}
