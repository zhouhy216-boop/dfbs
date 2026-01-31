package com.dfbs.app.modules.shipment;

/**
 * 发货类型: 正常发货、销售委托(公司成本)、生产委托(公司成本)、客户委托(收入+垫付)
 */
public enum ShipmentType {
    NORMAL,              // 正常发货
    SALES_DELEGATE,      // 销售委托 - 公司成本
    PRODUCTION_DELEGATE, // 生产委托 - 公司成本
    CUSTOMER_DELEGATE    // 客户委托 - 收入+垫付
}
