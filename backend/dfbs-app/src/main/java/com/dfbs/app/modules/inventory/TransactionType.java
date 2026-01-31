package com.dfbs.app.modules.inventory;

/**
 * 库存事务类型
 */
public enum TransactionType {
    INBOUND,         // 入库
    OUTBOUND_WO,     // 工单出库
    OUTBOUND_QUOTE,  // 报价单出库
    OUTBOUND_SPECIAL,// 特殊出库
    TRANSFER_OUT,    // 调拨出
    TRANSFER_IN,     // 调拨入
    RETURN           // 归还
}
