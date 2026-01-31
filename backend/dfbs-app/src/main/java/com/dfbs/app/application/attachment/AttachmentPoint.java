package com.dfbs.app.application.attachment;

/**
 * Point in the flow where an attachment is required.
 */
public enum AttachmentPoint {
    CONFIRM,           // Freight bill confirm (Bill Photo)
    SHIP_PICK_TICKET,  // Shipment ship (Pick Ticket)
    COMPLETE_RECEIPT,  // Shipment complete (Receipt)
    EXECUTE,           // HQ Transfer ship (Logistics Bill)
    CREATE,            // Damage record create (Damage Photo)
    SUBMIT             // Correction submit (mandatory attachment)
}
