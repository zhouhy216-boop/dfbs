package com.dfbs.app.modules.warehouse;

/** 补货申请单状态 */
public enum ReplenishStatus {
    DRAFT,      // 草稿
    PENDING_L1, // 待负责人审批
    PENDING_L2, // 待领导审批
    APPROVED,   // 通过/待落账
    REJECTED,   // 驳回
    COMPLETED   // 已落账
}
