package com.dfbs.app.application.importdata;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/** Excel row: machineNo, serialNo (required), modelName, customerName (optional refs). */
@Data
public class MachineImportRow {

    @ExcelProperty(index = 0)
    private String machineNo;

    @ExcelProperty(index = 1)
    private String serialNo;

    @ExcelProperty(index = 2)
    private String modelName;

    @ExcelProperty(index = 3)
    private String customerName;

    public boolean isBlank() {
        return (machineNo == null || machineNo.isBlank()) && (serialNo == null || serialNo.isBlank());
    }
}
