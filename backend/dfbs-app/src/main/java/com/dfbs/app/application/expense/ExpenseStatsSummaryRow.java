package com.dfbs.app.application.expense;

import com.alibaba.excel.annotation.ExcelProperty;
import com.dfbs.app.modules.quote.enums.Currency;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel row for Expense Stats Summary sheet (from ExpenseStatsItemDto).
 */
public class ExpenseStatsSummaryRow {

    @ExcelProperty("Group")
    private String groupKey;

    @ExcelProperty("Total (RMB)")
    private BigDecimal totalRmb;

    @ExcelProperty("Total Amount")
    private String totalAmount;

    @ExcelProperty("Submitted Amount")
    private String submittedAmount;

    @ExcelProperty("Approved Amount")
    private String approvedAmount;

    @ExcelProperty("Rejected Amount")
    private String rejectedAmount;

    @ExcelProperty("Est. Transport")
    private BigDecimal estTransport;

    @ExcelProperty("Est. Accommodation")
    private BigDecimal estAccommodation;

    @ExcelProperty("Real Transport")
    private BigDecimal realTransport;

    @ExcelProperty("Real Accommodation")
    private BigDecimal realAccommodation;

    public static String formatAmountMap(Map<Currency, BigDecimal> map) {
        if (map == null || map.isEmpty()) return "";
        return map.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue().compareTo(BigDecimal.ZERO) != 0)
                .map(e -> e.getKey().name() + ":" + e.getValue())
                .collect(Collectors.joining("; "));
    }

    public static ExpenseStatsSummaryRow from(ExpenseStatsItemDto dto) {
        ExpenseStatsSummaryRow row = new ExpenseStatsSummaryRow();
        row.setGroupKey(dto.getGroupKey());
        row.setTotalRmb(dto.getTotalRmb());
        row.setTotalAmount(formatAmountMap(dto.getTotalAmount()));
        row.setSubmittedAmount(formatAmountMap(dto.getSubmittedAmount()));
        row.setApprovedAmount(formatAmountMap(dto.getApprovedAmount()));
        row.setRejectedAmount(formatAmountMap(dto.getRejectedAmount()));
        row.setEstTransport(dto.getEstTransport());
        row.setEstAccommodation(dto.getEstAccommodation());
        row.setRealTransport(dto.getRealTransport());
        row.setRealAccommodation(dto.getRealAccommodation());
        return row;
    }

    public String getGroupKey() { return groupKey; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }
    public BigDecimal getTotalRmb() { return totalRmb; }
    public void setTotalRmb(BigDecimal totalRmb) { this.totalRmb = totalRmb; }
    public String getTotalAmount() { return totalAmount; }
    public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }
    public String getSubmittedAmount() { return submittedAmount; }
    public void setSubmittedAmount(String submittedAmount) { this.submittedAmount = submittedAmount; }
    public String getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(String approvedAmount) { this.approvedAmount = approvedAmount; }
    public String getRejectedAmount() { return rejectedAmount; }
    public void setRejectedAmount(String rejectedAmount) { this.rejectedAmount = rejectedAmount; }
    public BigDecimal getEstTransport() { return estTransport; }
    public void setEstTransport(BigDecimal estTransport) { this.estTransport = estTransport; }
    public BigDecimal getEstAccommodation() { return estAccommodation; }
    public void setEstAccommodation(BigDecimal estAccommodation) { this.estAccommodation = estAccommodation; }
    public BigDecimal getRealTransport() { return realTransport; }
    public void setRealTransport(BigDecimal realTransport) { this.realTransport = realTransport; }
    public BigDecimal getRealAccommodation() { return realAccommodation; }
    public void setRealAccommodation(BigDecimal realAccommodation) { this.realAccommodation = realAccommodation; }
}
