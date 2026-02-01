package com.dfbs.app.application.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhApproveReq {

    private Long requestId;
    private Boolean approved;
    private String comment;
}
