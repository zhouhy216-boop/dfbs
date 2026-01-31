package com.dfbs.app.application.freightbill;

import com.alibaba.excel.annotation.ExcelProperty;

import java.math.BigDecimal;

/** Excel row for merged freight bill export - Details sheet: Shipment No, Address, Amount. */
public class FreightBillDetailRow {

    @ExcelProperty("Shipment No")
    private String shipmentNo;

    @ExcelProperty("Address")
    private String address;

    @ExcelProperty("Amount")
    private BigDecimal amount;

    public static FreightBillDetailRow of(String shipmentNo, String address, BigDecimal amount) {
        FreightBillDetailRow row = new FreightBillDetailRow();
        row.setShipmentNo(shipmentNo);
        row.setAddress(address != null ? address : "");
        row.setAmount(amount != null ? amount : BigDecimal.ZERO);
        return row;
    }

    public String getShipmentNo() { return shipmentNo; }
    public void setShipmentNo(String shipmentNo) { this.shipmentNo = shipmentNo; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
