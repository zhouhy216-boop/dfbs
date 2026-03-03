package com.dfbs.app.application.bizperm;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One row for XLSX sheet 「操作点」. CN headers only for round-trip export.
 * 节点ID empty = 未归类 (unclassified).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogOpPointExportRow {

    @ExcelProperty("操作点ID")
    private Long id;

    @ExcelProperty("节点ID(空=未归类)")
    private Long nodeId;

    @ExcelProperty("权限键")
    private String permissionKey;

    @ExcelProperty("中文名称")
    private String cnName;

    @ExcelProperty("排序")
    private Integer sortOrder;

    @ExcelProperty("支持仅已处理(是/否)")
    private String handledOnlyYesNo;
}
