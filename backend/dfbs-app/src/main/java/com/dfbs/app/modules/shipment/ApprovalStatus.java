package com.dfbs.app.modules.shipment;

/**
 * 审批状态 (Future-proofing).
 */
public enum ApprovalStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}
