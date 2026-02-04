package com.dfbs.app.modules.workorder;

/** 工单状态 */
public enum WorkOrderStatus {
    PENDING,                 // 待受理
    ACCEPTED_BY_DISPATCHER,  // 已受理/待派单（调度员受理并关联客户）
    DISPATCHED,              // 已派单
    ACCEPTED,     // 已接单
    PROCESSING,   // 处理中
    PENDING_SIGN, // 待签字
    COMPLETED,    // 已完修
    CANCELLED     // 已取消
}
