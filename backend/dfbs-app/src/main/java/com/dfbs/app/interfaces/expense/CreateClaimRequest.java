package com.dfbs.app.interfaces.expense;

import java.util.List;

public record CreateClaimRequest(List<Long> expenseIds) {}
