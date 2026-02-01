package com.dfbs.app.modules.warehouse;

/** 入库 / 出库 / 补货入 / 补货出 */
public enum StockRecordType {
    INBOUND,      // 入库
    OUTBOUND,     // 出库
    REPLENISH_IN, // 补货入
    REPLENISH_OUT // 补货出
}
