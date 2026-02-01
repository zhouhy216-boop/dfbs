package com.dfbs.app.application.importdata;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/** Excel row: modelName, modelNo (required). */
@Data
public class MachineModelImportRow {

    @ExcelProperty(index = 0)
    private String modelName;

    @ExcelProperty(index = 1)
    private String modelNo;

    @ExcelProperty(index = 2)
    private String freightInfo;

    @ExcelProperty(index = 3)
    private String warrantyInfo;

    public boolean isBlank() {
        return (modelName == null || modelName.isBlank()) && (modelNo == null || modelNo.isBlank());
    }
}
