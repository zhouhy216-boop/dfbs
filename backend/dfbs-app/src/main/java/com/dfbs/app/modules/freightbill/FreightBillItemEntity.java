package com.dfbs.app.modules.freightbill;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "freight_bill_item")
@Data
public class FreightBillItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_id", nullable = false)
    private Long billId;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "shipment_no", length = 64)
    private String shipmentNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "financial_category", nullable = false, length = 32)
    private FinancialCategory financialCategory;

    @Column(name = "machine_model", length = 256)
    private String machineModel;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "line_total", precision = 19, scale = 4)
    private BigDecimal lineTotal;

    /** Reserved for future fees. JSON string. */
    @Column(name = "additional_charges", columnDefinition = "TEXT")
    private String additionalCharges;

    @Column(name = "remark", length = 500)
    private String remark;

    public FreightBillItemEntity() {}
}
