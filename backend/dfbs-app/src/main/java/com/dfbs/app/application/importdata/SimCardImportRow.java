package com.dfbs.app.application.importdata;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/** Excel row: cardNo (required), operator. */
@Data
public class SimCardImportRow {

    @ExcelProperty(index = 0)
    private String cardNo;

    @ExcelProperty(index = 1)
    private String operator;

    @ExcelProperty(index = 2)
    private String planInfo;

    public boolean isBlank() {
        return cardNo == null || cardNo.isBlank();
    }
}
