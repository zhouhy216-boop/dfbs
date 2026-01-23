package com.dfbs.app.modules.quote.enums;

import lombok.Getter;

@Getter
public enum QuoteExpenseType {
    REPAIR("维修费", "次"),
    ON_SITE("上门费", "次"),
    PARTS("配件费", "个"),
    PLATFORM("平台费", "点"),
    DATA_PLAN("流量费", "月"),
    STORAGE("仓储费", "次"),
    SHIPPING("运输费", "次"),
    PACKING("包装费", "次"),
    CONSTRUCTION("施工费", "次"),
    OTHER("其他", "次");

    private final String description;
    private final String defaultUnit; // 建议单位

    QuoteExpenseType(String description, String defaultUnit) {
        this.description = description;
        this.defaultUnit = defaultUnit;
    }
}
