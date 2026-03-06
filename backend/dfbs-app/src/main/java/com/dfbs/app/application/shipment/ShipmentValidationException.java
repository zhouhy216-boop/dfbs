package com.dfbs.app.application.shipment;

/**
 * Shipment step validation failure; controller/GlobalExceptionHandler maps to 400 + ErrorResult.
 */
public class ShipmentValidationException extends RuntimeException {

    public static final String MACHINE_CODE_PREFIX = "SHIPMENT_";

    public static final String SHIPMENT_MISSING_RECEIVER = MACHINE_CODE_PREFIX + "MISSING_RECEIVER";
    public static final String SHIPMENT_MISSING_DELIVERY_ADDRESS = MACHINE_CODE_PREFIX + "MISSING_DELIVERY_ADDRESS";
    public static final String SHIPMENT_MISSING_CARRIER = MACHINE_CODE_PREFIX + "MISSING_CARRIER";
    public static final String SHIPMENT_MISSING_LOGISTICS_NO = MACHINE_CODE_PREFIX + "MISSING_LOGISTICS_NO";
    public static final String SHIPMENT_MACHINE_NOT_FOUND = MACHINE_CODE_PREFIX + "MACHINE_NOT_FOUND";

    private final String machineCode;

    public ShipmentValidationException(String message, String machineCode) {
        super(message);
        this.machineCode = machineCode != null ? machineCode : "VALIDATION_ERROR";
    }

    public String getMachineCode() {
        return machineCode;
    }
}
