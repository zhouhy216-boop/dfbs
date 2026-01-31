package com.dfbs.app.modules.permission;

/**
 * 权限申请状态: 待处理 / 已退回 / 已同意 / 已拒绝
 */
public enum RequestStatus {
    PENDING,   // 待处理
    RETURNED,  // 已退回
    APPROVED,  // 已同意
    REJECTED   // 已拒绝
}
