package com.dfbs.app.application.repair;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel row for repair record import. Columns: Customer, SN, Model, Date, Issue, Resolution, Person, Status, OldNo.
 */
@Data
public class RepairRecordExcelRow {

    @ExcelProperty(index = 0)
    private String customerName;

    @ExcelProperty(index = 1)
    private String machineNo;

    @ExcelProperty(index = 2)
    private String machineModel;

    @ExcelProperty(index = 3)
    private String repairDateStr;  // yyyy-MM-dd or yyyy-MM-dd HH:mm

    @ExcelProperty(index = 4)
    private String issueDescription;

    @ExcelProperty(index = 5)
    private String resolution;

    @ExcelProperty(index = 6)
    private String personInCharge;

    @ExcelProperty(index = 7)
    private String warrantyStatus;  // IN_WARRANTY / OUT_WARRANTY or 在保/过保

    @ExcelProperty(index = 8)
    private String oldWorkOrderNo;

    public boolean isBlank() {
        return (customerName == null || customerName.isBlank())
                && (machineNo == null || machineNo.isBlank())
                && (machineModel == null || machineModel.isBlank())
                && (repairDateStr == null || repairDateStr.isBlank())
                && (issueDescription == null || issueDescription.isBlank())
                && (resolution == null || resolution.isBlank())
                && (personInCharge == null || personInCharge.isBlank())
                && (warrantyStatus == null || warrantyStatus.isBlank())
                && (oldWorkOrderNo == null || oldWorkOrderNo.isBlank());
    }
}
