package com.dfbs.app.application.freightbill;

import com.alibaba.excel.annotation.ExcelProperty;

import java.math.BigDecimal;

/** Excel row for merged freight bill export - Summary sheet: Bill No, Carrier, Amount. */
public class FreightBillSummaryRow {

    @ExcelProperty("Bill No")
    private String billNo;

    @ExcelProperty("Carrier")
    private String carrier;

    @ExcelProperty("Amount")
    private BigDecimal amount;

    public static FreightBillSummaryRow of(String billNo, String carrier, BigDecimal amount) {
        FreightBillSummaryRow row = new FreightBillSummaryRow();
        row.setBillNo(billNo);
        row.setCarrier(carrier);
        row.setAmount(amount != null ? amount : BigDecimal.ZERO);
        return row;
    }

    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
