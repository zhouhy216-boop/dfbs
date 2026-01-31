package com.dfbs.app.application.expense;

import com.alibaba.excel.annotation.ExcelProperty;
import com.dfbs.app.application.expense.dto.ExpenseDto;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Excel row for Expense Stats Details sheet (from ExpenseDto).
 */
public class ExpenseStatsDetailRow {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("Created At")
    private String createdAt;

    @ExcelProperty("Created By")
    private Long createdBy;

    @ExcelProperty("Expense Date")
    private String expenseDate;

    @ExcelProperty("Amount")
    private BigDecimal amount;

    @ExcelProperty("Currency")
    private String currency;

    @ExcelProperty("Expense Type")
    private String expenseType;

    @ExcelProperty("Description")
    private String description;

    @ExcelProperty("Status")
    private String status;

    @ExcelProperty("Quote ID")
    private Long quoteId;

    @ExcelProperty("Work Order ID")
    private Long workOrderId;

    @ExcelProperty("Trip Request ID")
    private Long tripRequestId;

    @ExcelProperty("Claim ID")
    private Long claimId;

    public static ExpenseStatsDetailRow from(ExpenseDto dto) {
        ExpenseStatsDetailRow row = new ExpenseStatsDetailRow();
        row.setId(dto.id());
        row.setCreatedAt(dto.createdAt() != null ? dto.createdAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
        row.setCreatedBy(dto.createdBy());
        row.setExpenseDate(dto.expenseDate() != null ? dto.expenseDate().toString() : null);
        row.setAmount(dto.amount());
        row.setCurrency(dto.currency() != null ? dto.currency().name() : null);
        row.setExpenseType(dto.expenseType() != null ? dto.expenseType().name() : null);
        row.setDescription(dto.description());
        row.setStatus(dto.status() != null ? dto.status().name() : null);
        row.setQuoteId(dto.quoteId());
        row.setWorkOrderId(dto.workOrderId());
        row.setTripRequestId(dto.tripRequestId());
        row.setClaimId(dto.claimId());
        return row;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public String getExpenseDate() { return expenseDate; }
    public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getExpenseType() { return expenseType; }
    public void setExpenseType(String expenseType) { this.expenseType = expenseType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getQuoteId() { return quoteId; }
    public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Long getTripRequestId() { return tripRequestId; }
    public void setTripRequestId(Long tripRequestId) { this.tripRequestId = tripRequestId; }
    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }
}
