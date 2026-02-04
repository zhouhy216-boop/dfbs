package com.dfbs.app.application.workorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderCreateReq {

    /** When set (from SmartReferenceSelect), links to customer master data. */
    private Long customerId;
    /** Snapshot name; required when customerId is null, else filled from customer when customerId present. */
    private String customerName;
    private String contactPerson;
    private String contactPhone;
    private String serviceAddress;
    private Long deviceModelId;
    private String issueDescription;
    private LocalDateTime appointmentTime;
}
