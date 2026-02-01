package com.dfbs.app.application.workorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderRecordReq {

    private String description;
    private String attachmentUrl;
}
