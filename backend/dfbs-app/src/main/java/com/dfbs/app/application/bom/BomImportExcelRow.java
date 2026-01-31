package com.dfbs.app.application.bom;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel row for BOM import. Columns: IndexNo, PartName, Spec (optional), DrawingNo (optional), Quantity, IsOptional, Remark.
 */
@Data
public class BomImportExcelRow {

    @ExcelProperty(index = 0)
    private String indexNo;

    @ExcelProperty(index = 1)
    private String partName;

    @ExcelProperty(index = 2)
    private String spec;

    @ExcelProperty(index = 3)
    private String drawingNo;

    @ExcelProperty(index = 4)
    private String quantity;

    @ExcelProperty(index = 5)
    private String isOptional;  // true/false or 是/否

    @ExcelProperty(index = 6)
    private String remark;

    public boolean isBlank() {
        return (indexNo == null || indexNo.isBlank())
                && (partName == null || partName.isBlank())
                && (spec == null || spec.isBlank())
                && (drawingNo == null || drawingNo.isBlank())
                && (quantity == null || quantity.isBlank())
                && (isOptional == null || isOptional.isBlank())
                && (remark == null || remark.isBlank());
    }
}
