package com.dfbs.app.modules.freightbill;

/**
 * 运单状态: 草稿 / 已确认 / 已结算
 */
public enum FreightBillStatus {
    DRAFT,      // 草稿
    CONFIRMED,  // 已确认
    SETTLED,    // 已结算
    VOID        // 作废（数据更正）
}
