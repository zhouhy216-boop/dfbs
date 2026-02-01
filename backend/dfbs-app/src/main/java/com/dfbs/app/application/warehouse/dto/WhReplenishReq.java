package com.dfbs.app.application.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhReplenishReq {

    private Long targetWarehouseId;
    private String partNo;
    private Integer quantity;
    private String reason;
}
