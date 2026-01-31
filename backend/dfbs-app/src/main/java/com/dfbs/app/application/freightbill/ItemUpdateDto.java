package com.dfbs.app.application.freightbill;

import java.math.BigDecimal;

/**
 * Input for updating a freight bill item: unit price and optional remark.
 */
public record ItemUpdateDto(
        Long itemId,
        BigDecimal unitPrice,
        String remark
) {}
