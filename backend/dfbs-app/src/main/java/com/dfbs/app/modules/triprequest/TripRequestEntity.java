package com.dfbs.app.modules.triprequest;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trip_request")
public class TripRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TripRequestStatus status = TripRequestStatus.DRAFT;

    @NotNull
    @Column(name = "city", nullable = false, length = 256)
    private String city;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @Column(name = "purpose", nullable = false, columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "est_transport_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal estTransportCost = BigDecimal.ZERO;

    @Column(name = "est_accommodation_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal estAccommodationCost = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency = "CNY";

    @Column(name = "work_order_id")
    private Long workOrderId;

    @Column(name = "independent_reason", columnDefinition = "TEXT")
    private String independentReason;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "approver_leader_id")
    private Long approverLeaderId;

    @Column(name = "approve_leader_time")
    private OffsetDateTime approveLeaderTime;

    @Column(name = "approver_finance_id")
    private Long approverFinanceId;

    @Column(name = "approve_finance_time")
    private OffsetDateTime approveFinanceTime;

    public TripRequestEntity() {}

    public Long getId() { return id; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public TripRequestStatus getStatus() { return status; }
    public void setStatus(TripRequestStatus status) { this.status = status; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public BigDecimal getEstTransportCost() { return estTransportCost; }
    public void setEstTransportCost(BigDecimal estTransportCost) { this.estTransportCost = estTransportCost; }
    public BigDecimal getEstAccommodationCost() { return estAccommodationCost; }
    public void setEstAccommodationCost(BigDecimal estAccommodationCost) { this.estAccommodationCost = estAccommodationCost; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getIndependentReason() { return independentReason; }
    public void setIndependentReason(String independentReason) { this.independentReason = independentReason; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public Long getApproverLeaderId() { return approverLeaderId; }
    public void setApproverLeaderId(Long approverLeaderId) { this.approverLeaderId = approverLeaderId; }
    public OffsetDateTime getApproveLeaderTime() { return approveLeaderTime; }
    public void setApproveLeaderTime(OffsetDateTime approveLeaderTime) { this.approveLeaderTime = approveLeaderTime; }
    public Long getApproverFinanceId() { return approverFinanceId; }
    public void setApproverFinanceId(Long approverFinanceId) { this.approverFinanceId = approverFinanceId; }
    public OffsetDateTime getApproveFinanceTime() { return approveFinanceTime; }
    public void setApproveFinanceTime(OffsetDateTime approveFinanceTime) { this.approveFinanceTime = approveFinanceTime; }
}
