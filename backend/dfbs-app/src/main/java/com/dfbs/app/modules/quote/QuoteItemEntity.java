package com.dfbs.app.modules.quote;

import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteItemWarehouse;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "quote_item")
@Data
public class QuoteItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(name = "line_order", nullable = false)
    private Integer lineOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type", nullable = false, length = 32)
    private QuoteExpenseType expenseType;

    @Column(name = "fee_type_id")
    private Long feeTypeId;  // FK to FeeTypeEntity, Nullable

    @Column(name = "part_id")
    private Long partId;  // FK to PartEntity, Nullable

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "original_part_name", length = 500)
    private String originalPartName;

    @Column(name = "spec", length = 500)
    private String spec;

    @Column(name = "unit", length = 32)
    private String unit;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "standard_price", precision = 19, scale = 4)
    private BigDecimal standardPrice;  // Snapshot of Part.salesPrice

    @Column(name = "is_price_deviated", nullable = false)
    private Boolean isPriceDeviated = false;  // Flag when unitPrice != standardPrice

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse", length = 32)
    private QuoteItemWarehouse warehouse;

    @Column(name = "remark", length = 1000)
    private String remark;

    /** Audit JSON for contract price source, e.g. {"contractId":123,"strategy":"LOWEST_PRICE"}. */
    @Column(name = "price_source_info", columnDefinition = "TEXT")
    private String priceSourceInfo;

    /** Required when user overrides suggested/standard price (manual price reason). */
    @Column(name = "manual_price_reason", columnDefinition = "TEXT")
    private String manualPriceReason;

    // 预留字段
    @Column(name = "attr1", length = 500)
    private String attr1;

    @Column(name = "attr2", length = 500)
    private String attr2;

    @Column(name = "attr3", length = 500)
    private String attr3;

    @Column(name = "attr4", length = 500)
    private String attr4;

    @Column(name = "attr5", length = 500)
    private String attr5;

    @Column(name = "attr6", length = 500)
    private String attr6;

    @Column(name = "attr7", length = 500)
    private String attr7;

    @Column(name = "attr8", length = 500)
    private String attr8;

    @Column(name = "attr9", length = 500)
    private String attr9;

    @Column(name = "attr10", length = 500)
    private String attr10;

    public QuoteItemEntity() {}
}
