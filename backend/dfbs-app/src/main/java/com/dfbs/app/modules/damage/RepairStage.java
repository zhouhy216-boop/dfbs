package com.dfbs.app.modules.damage;

/**
 * 维修阶段: 已返厂 / 维修中 / 已结算
 */
public enum RepairStage {
    RETURNED,   // 已返厂
    REPAIRING,  // 维修中/已下单
    SETTLED     // 已结算
}
