package com.dfbs.app.modules.expense;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "claim")
public class ClaimEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_no", nullable = false, unique = true, length = 64)
    private String claimNo;

    @Column(name = "title", length = 256)
    private String title;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 8)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ClaimStatus status = ClaimStatus.DRAFT;

    @Column(name = "submitter_id")
    private Long submitterId;

    @Column(name = "submit_time")
    private OffsetDateTime submitTime;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "approve_time")
    private OffsetDateTime approveTime;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @OneToMany(mappedBy = "claim", cascade = {}, fetch = FetchType.LAZY)
    private List<ExpenseEntity> expenses = new ArrayList<>();

    public ClaimEntity() {}

    public Long getId() { return id; }
    public String getClaimNo() { return claimNo; }
    public void setClaimNo(String claimNo) { this.claimNo = claimNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }
    public Long getSubmitterId() { return submitterId; }
    public void setSubmitterId(Long submitterId) { this.submitterId = submitterId; }
    public OffsetDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(OffsetDateTime submitTime) { this.submitTime = submitTime; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public OffsetDateTime getApproveTime() { return approveTime; }
    public void setApproveTime(OffsetDateTime approveTime) { this.approveTime = approveTime; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public List<ExpenseEntity> getExpenses() { return expenses; }
    public void setExpenses(List<ExpenseEntity> expenses) { this.expenses = expenses != null ? expenses : new ArrayList<>(); }
}
