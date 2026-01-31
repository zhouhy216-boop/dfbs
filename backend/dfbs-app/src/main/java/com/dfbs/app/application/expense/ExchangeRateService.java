package com.dfbs.app.application.expense;

import com.dfbs.app.modules.quote.enums.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

/**
 * MVP: hardcoded rates to RMB. USD: 7.2, JPY: 0.05, CNY: 1.0.
 */
@Service
public class ExchangeRateService {

    private static final Map<Currency, BigDecimal> RATES_TO_RMB = new EnumMap<>(Currency.class);

    static {
        RATES_TO_RMB.put(Currency.CNY, BigDecimal.ONE);
        RATES_TO_RMB.put(Currency.USD, new BigDecimal("7.2"));
        RATES_TO_RMB.put(Currency.JPY, new BigDecimal("0.05"));
    }

    public BigDecimal convertToRmb(BigDecimal amount, Currency currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if (currency == null) currency = Currency.CNY;
        BigDecimal rate = RATES_TO_RMB.getOrDefault(currency, BigDecimal.ONE);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
