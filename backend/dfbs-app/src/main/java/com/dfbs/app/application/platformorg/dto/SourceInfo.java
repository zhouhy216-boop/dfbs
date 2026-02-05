package com.dfbs.app.application.platformorg.dto;

/**
 * Source traceability for platform org: from Sales/Service App or Manual.
 */
public record SourceInfo(
        String type,
        String applicationNo,
        String applicantName,
        String plannerName,
        String adminName
) {
    public static final String TYPE_LEGACY = "LEGACY";
    public static final String TYPE_SALES = "SALES";
    public static final String TYPE_SERVICE = "SERVICE";
    public static final String TYPE_MANUAL = "MANUAL";
}
