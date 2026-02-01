package com.dfbs.app.application.workorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderCreateReq {

    private String customerName;
    private String contactPerson;
    private String contactPhone;
    private String serviceAddress;
    private Long deviceModelId;
    private String issueDescription;
    private LocalDateTime appointmentTime;
}
