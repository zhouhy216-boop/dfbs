package com.dfbs.app.modules.platformaccount;

public enum PlatformAccountApplicationStatus {
    DRAFT,
    PENDING,         // 退回申请人（驳回后 SERVICE）
    PENDING_PLANNER,
    PENDING_CONFIRM, // 退回营企（驳回后 FACTORY）
    PENDING_ADMIN,
    APPROVED,
    REJECTED,
    CLOSED   // 已关闭：流程终止，营企可重新打开并提交
}
