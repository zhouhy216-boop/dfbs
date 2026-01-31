package com.dfbs.app.application.correction;

/**
 * Strategy for voiding and cloning a target entity (Quote, Payment, Expense, FreightBill).
 */
public interface CorrectionExecutor {

    /** Void/cancel the old record (id). */
    void voidOld(Long id);

    /** Create a new record from the old one, optionally applying changes. Returns new entity id. */
    Long createNew(Long oldId, String changesJson, Long createdBy);
}
