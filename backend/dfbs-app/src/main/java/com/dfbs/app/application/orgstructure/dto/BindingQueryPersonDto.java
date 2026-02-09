package com.dfbs.app.application.orgstructure.dto;

/** Contact info for query API: org+position -> people. */
public record BindingQueryPersonDto(
        Long id,
        String name,
        String phone,
        String email,
        Boolean isPartTime
) {}
