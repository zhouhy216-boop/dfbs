package com.dfbs.app.modules.inventory;

/**
 * 调拨单状态
 */
public enum TransferStatus {
    PENDING,    // 待发货
    IN_TRANSIT, // 在途
    COMPLETED,  // 已完成
    REJECTED    // 已拒绝
}
