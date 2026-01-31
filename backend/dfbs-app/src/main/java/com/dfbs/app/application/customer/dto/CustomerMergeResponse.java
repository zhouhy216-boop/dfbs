package com.dfbs.app.application.customer.dto;

import com.dfbs.app.modules.customer.CustomerEntity;

public record CustomerMergeResponse(
        Long mergeLogId,
        CustomerEntity targetCustomer
) {}
