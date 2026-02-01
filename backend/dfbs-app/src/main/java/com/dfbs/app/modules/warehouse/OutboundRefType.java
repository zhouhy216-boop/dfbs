package com.dfbs.app.modules.warehouse;

/** 出库关联: 工单(保内) / 报价单(保外) */
public enum OutboundRefType {
    WORK_ORDER, // 工单-保内
    QUOTE       // 报价单-保外
}
