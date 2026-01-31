package com.dfbs.app.application.shipment;

import java.util.List;

/**
 * Input for machine ID generation: either specificNos or startNo+count for sequential.
 */
public record MachineEntryDto(
        String model,
        String startNo,
        String endNo,
        Integer count,
        List<String> specificNos
) {}
