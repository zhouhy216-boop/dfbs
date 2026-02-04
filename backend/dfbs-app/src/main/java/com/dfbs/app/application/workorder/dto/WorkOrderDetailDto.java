package com.dfbs.app.application.workorder.dto;

import com.dfbs.app.modules.workorder.WorkOrderEntity;
import com.dfbs.app.modules.workorder.WorkOrderPartEntity;
import com.dfbs.app.modules.workorder.WorkOrderRecordEntity;

import java.util.List;

/** Full work order detail: main entity + records + parts. customerNameDisplay is resolved from master when customerId is set. */
public record WorkOrderDetailDto(
    WorkOrderEntity workOrder,
    List<WorkOrderRecordEntity> records,
    List<WorkOrderPartEntity> parts,
    String customerNameDisplay
) {}
