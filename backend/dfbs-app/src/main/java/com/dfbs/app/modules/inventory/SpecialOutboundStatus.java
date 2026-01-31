package com.dfbs.app.modules.inventory;

/**
 * 特殊出库申请状态
 */
public enum SpecialOutboundStatus {
    PENDING_APPROVAL, // 待审批
    APPROVED,         // 已批准
    REJECTED,        // 已拒绝
    COMPLETED        // 已执行
}
