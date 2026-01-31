package com.dfbs.app.modules.quote.void_;

/**
 * Role of the user applying or performing a void action.
 */
public enum VoidRequesterRole {
    INITIATOR,           // Quote creator/assignee (pre–finance-confirm can direct void)
    CUSTOMER_CONFIRMER,  // Submits quote to Finance (pre–finance can only apply → wait Initiator)
    FINANCE,             // Finance user (post–finance normal can direct void)
    COLLECTOR,           // Collector (applies for void; in strict control only Collector may apply)
    LEADER               // Business line leader (approves in strict control flow)
}
