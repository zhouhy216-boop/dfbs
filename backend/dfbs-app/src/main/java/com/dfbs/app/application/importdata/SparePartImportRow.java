package com.dfbs.app.application.importdata;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/** Excel row: partNo, name (required). */
@Data
public class SparePartImportRow {

    @ExcelProperty(index = 0)
    private String partNo;

    @ExcelProperty(index = 1)
    private String name;

    @ExcelProperty(index = 2)
    private String spec;

    @ExcelProperty(index = 3)
    private String unit;

    public boolean isBlank() {
        return (partNo == null || partNo.isBlank()) && (name == null || name.isBlank());
    }
}
