package com.dfbs.app.modules.expense;

import com.dfbs.app.modules.quote.enums.Currency;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "expense")
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 8)
    private Currency currency = Currency.CNY;

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type", length = 32)
    private ExpenseType expenseType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Column(name = "quote_id")
    private Long quoteId;

    @Column(name = "work_order_id")
    private Long workOrderId;

    @Column(name = "inventory_outbound_id")
    private Long inventoryOutboundId;

    @Column(name = "trip_request_id")
    private Long tripRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id")
    private ClaimEntity claim;

    public ExpenseEntity() {}

    public Long getId() { return id; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public ExpenseType getExpenseType() { return expenseType; }
    public void setExpenseType(ExpenseType expenseType) { this.expenseType = expenseType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ExpenseStatus getStatus() { return status; }
    public void setStatus(ExpenseStatus status) { this.status = status; }
    public Long getQuoteId() { return quoteId; }
    public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Long getInventoryOutboundId() { return inventoryOutboundId; }
    public void setInventoryOutboundId(Long inventoryOutboundId) { this.inventoryOutboundId = inventoryOutboundId; }
    public Long getTripRequestId() { return tripRequestId; }
    public void setTripRequestId(Long tripRequestId) { this.tripRequestId = tripRequestId; }
    public ClaimEntity getClaim() { return claim; }
    public void setClaim(ClaimEntity claim) { this.claim = claim; }
}
