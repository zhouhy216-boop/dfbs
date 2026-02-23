package com.dfbs.app.application.dicttype;

/**
 * Thrown when a dictionary type is not found by typeCode. Handler returns 404 with machineCode DICT_TYPE_NOT_FOUND.
 */
public class DictTypeNotFoundException extends RuntimeException {

    public static final String MACHINE_CODE = "DICT_TYPE_NOT_FOUND";

    public DictTypeNotFoundException(String message) {
        super(message);
    }
}
