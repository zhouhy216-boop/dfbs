package com.dfbs.app.modules.workorder;

/** 工单状态 */
public enum WorkOrderStatus {
    PENDING,      // 待受理
    DISPATCHED,   // 已派单
    ACCEPTED,     // 已接单
    PROCESSING,   // 处理中
    PENDING_SIGN, // 待签字
    COMPLETED,    // 已完修
    CANCELLED     // 已取消
}
