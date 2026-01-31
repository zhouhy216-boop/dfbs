package com.dfbs.app.application.masterdata;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel row for part import. Columns: Name, Spec, Price, DrawingNo (optional).
 */
@Data
public class PartImportExcelRow {

    @ExcelProperty(index = 0)
    private String name;

    @ExcelProperty(index = 1)
    private String spec;

    @ExcelProperty(index = 2)
    private String price;  // parsed to BigDecimal

    @ExcelProperty(index = 3)
    private String drawingNo;  // optional

    public boolean isBlank() {
        return (name == null || name.isBlank())
                && (spec == null || spec.isBlank())
                && (price == null || price.isBlank())
                && (drawingNo == null || drawingNo.isBlank());
    }
}
