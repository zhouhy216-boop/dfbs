package com.dfbs.app.modules.quote.enums;

public enum WorkflowAction {
    SUBMIT,           // Customer Confirmer submits to Finance
    APPROVE,          // Finance approves
    REJECT,           // Finance rejects (return to customer confirmer)
    ASSIGN_COLLECTOR, // Finance assigns collector
    CONFIRM_PAYMENT,  // Finance confirms a payment record
    VOID,             // Void (application or direct)
    FALLBACK          // Return to previous step
}
