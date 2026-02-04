package com.dfbs.app.application.workorder.dto;

import com.dfbs.app.modules.workorder.WorkOrderStatus;
import com.dfbs.app.modules.workorder.WorkOrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Work order row for list APIs; customerName is resolved from master when customerId is set. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderListDto {
    private Long id;
    private String orderNo;
    private WorkOrderType type;
    private WorkOrderStatus status;
    private Long customerId;     // for pre-fill in Accept modal
    private String customerName; // resolved from customer master when customerId present
    private String contactPerson;
    private String contactPhone;
    private String serviceAddress;
    private Long serviceManagerId;
    private LocalDateTime createdAt;
}
