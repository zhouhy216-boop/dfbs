package com.dfbs.app.modules.quote.enums;

public enum QuoteVoidStatus {
    NONE,      // Default, no void application
    APPLYING,  // In progress, waiting for approval
    VOIDED,    // Approved/Direct voided
    REJECTED   // Rejected by finance
}
