package com.dfbs.app.application.importdata;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/** Flat Excel row for BOM import: Model Name, Version, Part No, Qty. Rows grouped by Model + Version. */
@Data
public class ModelPartListImportRow {

    @ExcelProperty(index = 0)
    private String modelName;

    @ExcelProperty(index = 1)
    private String version;

    @ExcelProperty(index = 2)
    private String partNo;

    @ExcelProperty(index = 3)
    private Integer quantity;

    public boolean isBlank() {
        return (modelName == null || modelName.isBlank())
                && (version == null || version.isBlank())
                && (partNo == null || partNo.isBlank());
    }
}
