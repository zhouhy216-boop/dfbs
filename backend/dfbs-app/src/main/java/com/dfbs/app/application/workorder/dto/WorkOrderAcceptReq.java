package com.dfbs.app.application.workorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request for dispatcher to accept a PENDING work order and link customer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderAcceptReq {

    /** Work order id. */
    private Long id;

    /** Link to customer master data when user picked from list. */
    private Long customerId;
    /** When user typed free text: resolve by exact name or create temp (lazy). */
    private String customerName;

    /** Optional: update contact person. */
    private String contactPerson;

    /** Optional: update contact phone. */
    private String contactPhone;

    /** Optional: update service address. */
    private String serviceAddress;

    /** Optional: update issue description. */
    private String issueDescription;

    /** Optional: update appointment time. */
    private LocalDateTime appointmentTime;
}
