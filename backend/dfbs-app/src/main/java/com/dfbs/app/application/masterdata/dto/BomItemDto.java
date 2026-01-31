package com.dfbs.app.application.masterdata.dto;

/** One BOM row for draft creation. */
public record BomItemDto(String partNo, String name, Integer quantity, String remark) {
    public BomItemDto {
        quantity = quantity != null ? quantity : 1;
    }
}
