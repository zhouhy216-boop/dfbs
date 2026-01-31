package com.dfbs.app.interfaces.payment;

import java.util.List;

public record BindPaymentsRequest(List<Long> paymentIds) {}
