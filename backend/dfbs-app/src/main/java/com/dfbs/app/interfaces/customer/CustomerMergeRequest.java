package com.dfbs.app.interfaces.customer;

import java.util.Map;

public record CustomerMergeRequest(
        Long targetId,
        Long sourceId,
        Map<String, Object> fieldOverrides,
        String mergeReason,
        String operatorId
) {}
