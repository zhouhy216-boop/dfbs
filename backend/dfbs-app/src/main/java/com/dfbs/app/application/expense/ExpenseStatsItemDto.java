package com.dfbs.app.application.expense;

import com.dfbs.app.modules.quote.enums.Currency;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ExpenseStatsItemDto {

    private String groupKey;

    /** Amounts by currency (e.g. total across all statuses). */
    private Map<Currency, BigDecimal> totalAmount = new HashMap<>();
    private Map<Currency, BigDecimal> submittedAmount = new HashMap<>();
    private Map<Currency, BigDecimal> approvedAmount = new HashMap<>();
    private Map<Currency, BigDecimal> rejectedAmount = new HashMap<>();

    /** Sum in RMB (converted). */
    private BigDecimal totalRmb = BigDecimal.ZERO;

    /** Trip-specific: budget from TripRequest. */
    private BigDecimal estTransport = BigDecimal.ZERO;
    private BigDecimal estAccommodation = BigDecimal.ZERO;
    /** Trip-specific: real from Expenses (TRANSPORT / ACCOMMODATION). */
    private BigDecimal realTransport = BigDecimal.ZERO;
    private BigDecimal realAccommodation = BigDecimal.ZERO;

    public String getGroupKey() { return groupKey; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }
    public Map<Currency, BigDecimal> getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Map<Currency, BigDecimal> totalAmount) { this.totalAmount = totalAmount != null ? totalAmount : new HashMap<>(); }
    public Map<Currency, BigDecimal> getSubmittedAmount() { return submittedAmount; }
    public void setSubmittedAmount(Map<Currency, BigDecimal> submittedAmount) { this.submittedAmount = submittedAmount != null ? submittedAmount : new HashMap<>(); }
    public Map<Currency, BigDecimal> getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(Map<Currency, BigDecimal> approvedAmount) { this.approvedAmount = approvedAmount != null ? approvedAmount : new HashMap<>(); }
    public Map<Currency, BigDecimal> getRejectedAmount() { return rejectedAmount; }
    public void setRejectedAmount(Map<Currency, BigDecimal> rejectedAmount) { this.rejectedAmount = rejectedAmount != null ? rejectedAmount : new HashMap<>(); }
    public BigDecimal getTotalRmb() { return totalRmb; }
    public void setTotalRmb(BigDecimal totalRmb) { this.totalRmb = totalRmb != null ? totalRmb : BigDecimal.ZERO; }
    public BigDecimal getEstTransport() { return estTransport; }
    public void setEstTransport(BigDecimal estTransport) { this.estTransport = estTransport != null ? estTransport : BigDecimal.ZERO; }
    public BigDecimal getEstAccommodation() { return estAccommodation; }
    public void setEstAccommodation(BigDecimal estAccommodation) { this.estAccommodation = estAccommodation != null ? estAccommodation : BigDecimal.ZERO; }
    public BigDecimal getRealTransport() { return realTransport; }
    public void setRealTransport(BigDecimal realTransport) { this.realTransport = realTransport != null ? realTransport : BigDecimal.ZERO; }
    public BigDecimal getRealAccommodation() { return realAccommodation; }
    public void setRealAccommodation(BigDecimal realAccommodation) { this.realAccommodation = realAccommodation != null ? realAccommodation : BigDecimal.ZERO; }
}
