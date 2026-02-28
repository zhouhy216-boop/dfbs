package com.dfbs.app.application.account;

/**
 * Thrown when creating an account for an org person that is already bound to another account (1:1).
 * Controller should return 400 with machineCode ACCTPERM_PERSON_ALREADY_BOUND.
 */
public class PersonAlreadyBoundException extends RuntimeException {

    public static final String MACHINE_CODE = "ACCTPERM_PERSON_ALREADY_BOUND";

    public PersonAlreadyBoundException(String message) {
        super(message != null ? message : "该人员已绑定账号");
    }
}
