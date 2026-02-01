package com.dfbs.app.application.workorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDispatchReq {

    private Long workOrderId;
    private Long serviceManagerId;
}
