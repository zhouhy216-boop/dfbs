package com.dfbs.app.modules.freightbill;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "freight_bill")
@Data
public class FreightBillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_no", nullable = false, unique = true, length = 64)
    private String billNo;

    @Column(name = "carrier_id")
    private Long carrierId;

    @Column(name = "carrier", length = 256)
    private String carrier;

    @Column(name = "period", length = 32)
    private String period;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private FreightBillStatus status = FreightBillStatus.DRAFT;

    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "attachment_url", length = 512)
    private String attachmentUrl;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "auditor_id")
    private Long auditorId;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "creator_id")
    private Long creatorId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", insertable = false, updatable = false)
    private List<FreightBillItemEntity> items = new ArrayList<>();

    public FreightBillEntity() {}
}
