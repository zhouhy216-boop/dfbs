package com.dfbs.app.application.invoice.dto;

import lombok.Data;

import java.util.List;

@Data
public class InvoiceApplicationCreateRequest {
    private List<InvoiceGroupRequest> groups;
    private String invoiceTitle;
    private String taxId;
    private String address;
    private String phone;
    private String bankName;
    private String bankAccount;
    private String email;
}
