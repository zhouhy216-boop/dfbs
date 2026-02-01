package com.dfbs.app.application.importdata;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/** Excel row: contractNo, customerName (required). */
@Data
public class ContractImportRow {

    @ExcelProperty(index = 0)
    private String contractNo;

    @ExcelProperty(index = 1)
    private String customerName;

    @ExcelProperty(index = 2)
    private String startDateStr;

    @ExcelProperty(index = 3)
    private String endDateStr;

    public boolean isBlank() {
        return (contractNo == null || contractNo.isBlank()) && (customerName == null || customerName.isBlank());
    }
}
