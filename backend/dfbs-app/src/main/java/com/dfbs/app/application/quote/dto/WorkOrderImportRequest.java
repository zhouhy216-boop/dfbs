package com.dfbs.app.application.quote.dto;

import java.util.List;

public record WorkOrderImportRequest(
        String workOrderNo,  // Required
        Long customerId,
        String customerName,
        String recipient,
        String phone,
        String address,
        String machineInfo,
        Boolean isOnsite,
        List<PartInfo> parts,
        Long serviceManagerUserId,  // Required
        Long collectorUserId  // Optional, for payment collection
) {
    public record PartInfo(
            String partName,
            Integer qty
    ) {}
}
