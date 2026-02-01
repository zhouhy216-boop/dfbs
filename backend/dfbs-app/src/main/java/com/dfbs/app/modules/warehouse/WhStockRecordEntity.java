package com.dfbs.app.modules.warehouse;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 库存流水/日志. quantity is change amount (e.g. -5 or +10); refType/refNo for outbound (OrderNo/QuoteNo).
 */
@Entity
@Table(name = "wh_stock_record")
@Getter
@Setter
public class WhStockRecordEntity extends BaseAuditEntity {

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "part_no", nullable = false, length = 128)
    private String partNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StockRecordType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", length = 32)
    private OutboundRefType refType;

    @Column(name = "ref_no", length = 64)
    private String refNo;

    @Column(length = 500)
    private String remark;
}
