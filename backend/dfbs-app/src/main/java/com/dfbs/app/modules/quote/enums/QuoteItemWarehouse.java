package com.dfbs.app.modules.quote.enums;

import lombok.Getter;

@Getter
public enum QuoteItemWarehouse {
    HEADQUARTERS("总部"),
    LOCAL("本地/随车");

    private final String label;

    QuoteItemWarehouse(String label) {
        this.label = label;
    }
}
