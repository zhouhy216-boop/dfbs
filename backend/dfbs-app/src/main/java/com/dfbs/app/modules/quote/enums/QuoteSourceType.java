package com.dfbs.app.modules.quote.enums;

import lombok.Getter;

@Getter
public enum QuoteSourceType {
    WORK_ORDER("WO"),
    PLATFORM_FEE("PF"),
    ENTRUST_SHIPMENT("ES"),
    MANUAL("GN"); // General

    private final String prefix;

    QuoteSourceType(String prefix) {
        this.prefix = prefix;
    }
}
