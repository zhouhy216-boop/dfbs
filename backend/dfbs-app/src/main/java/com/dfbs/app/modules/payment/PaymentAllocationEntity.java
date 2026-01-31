package com.dfbs.app.modules.payment;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_allocation")
public class PaymentAllocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEntity payment;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(name = "allocated_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(name = "period", length = 64)
    private String period;

    public PaymentAllocationEntity() {}

    public Long getId() { return id; }
    public PaymentEntity getPayment() { return payment; }
    public void setPayment(PaymentEntity payment) { this.payment = payment; }
    public Long getQuoteId() { return quoteId; }
    public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
