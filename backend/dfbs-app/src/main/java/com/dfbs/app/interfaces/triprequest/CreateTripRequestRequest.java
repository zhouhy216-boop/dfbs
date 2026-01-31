package com.dfbs.app.interfaces.triprequest;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTripRequestRequest(
        String city,
        LocalDate startDate,
        LocalDate endDate,
        String purpose,
        BigDecimal estTransportCost,
        BigDecimal estAccommodationCost,
        String currency,
        Long workOrderId,
        String independentReason
) {}
