package com.dfbs.app.application.contractprice;

import com.dfbs.app.modules.quote.enums.Currency;

import java.math.BigDecimal;

/**
 * Result of contract price suggestion: price, currency, and audit sourceInfo JSON.
 */
public class PriceSuggestionDto {

    private BigDecimal price;
    private Currency currency;
    private String sourceInfo;  // e.g. {"contractId":123,"strategy":"LOWEST_PRICE"}

    public static PriceSuggestionDto of(BigDecimal price, Currency currency, String sourceInfo) {
        PriceSuggestionDto dto = new PriceSuggestionDto();
        dto.setPrice(price);
        dto.setCurrency(currency);
        dto.setSourceInfo(sourceInfo);
        return dto;
    }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public String getSourceInfo() { return sourceInfo; }
    public void setSourceInfo(String sourceInfo) { this.sourceInfo = sourceInfo; }
}
