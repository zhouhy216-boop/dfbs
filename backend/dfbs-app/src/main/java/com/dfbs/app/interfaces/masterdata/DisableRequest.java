package com.dfbs.app.interfaces.masterdata;

/**
 * Request body for disable (soft delete) endpoints. updatedBy is optional.
 */
public record DisableRequest(String updatedBy) {}
