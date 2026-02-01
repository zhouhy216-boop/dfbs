package com.dfbs.app.application.warehouse.dto;

import com.dfbs.app.modules.warehouse.OutboundRefType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhOutboundReq {

    private Long warehouseId;
    private String partNo;
    private Integer quantity;
    private OutboundRefType refType;
    private String refNo;
    private String remark;
}
