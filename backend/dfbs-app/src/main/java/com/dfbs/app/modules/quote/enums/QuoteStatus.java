package com.dfbs.app.modules.quote.enums;

public enum QuoteStatus {
    DRAFT,                // Editable
    APPROVAL_PENDING,     // Submitted to Finance
    RETURNED,             // Sent back by Finance
    CONFIRMED,            // Finance approved, ready for collection
    PARTIAL_PAID,         // Partially paid (paymentStatus = PARTIAL)
    PAID,                 // Fully paid (paymentStatus = PAID)
    VOID_AUDIT_PENDING,   // Generic pending (e.g. wait for initiator)
    VOID_AUDIT_INITIATOR, // Wait for Initiator
    VOID_AUDIT_FINANCE,   // Wait for Finance
    VOID_AUDIT_LEADER,    // Wait for Leader
    CANCELLED             // Final void state
}
