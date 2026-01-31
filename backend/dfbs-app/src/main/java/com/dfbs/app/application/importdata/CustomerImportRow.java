package com.dfbs.app.application.importdata;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel row for customer import. Columns: Name (required), Code (optional).
 */
@Data
public class CustomerImportRow {

    @ExcelProperty(index = 0)
    private String name;

    @ExcelProperty(index = 1)
    private String code;

    public boolean isBlank() {
        return (name == null || name.isBlank()) && (code == null || code.isBlank());
    }
}
