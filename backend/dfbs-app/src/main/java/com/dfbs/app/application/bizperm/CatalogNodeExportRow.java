package com.dfbs.app.application.bizperm;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One row for XLSX sheet 「节点」. CN headers only for round-trip export.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogNodeExportRow {

    @ExcelProperty("节点ID")
    private Long id;

    @ExcelProperty("父节点ID")
    private Long parentId;

    @ExcelProperty("中文名称")
    private String cnName;

    @ExcelProperty("排序")
    private Integer sortOrder;
}
