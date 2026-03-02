package com.dfbs.app.application.bizperm;

/**
 * Business module catalog validation / business rule failure. Controller maps to 400 + ErrorResult.
 */
public class BizPermCatalogException extends RuntimeException {

    private final String machineCode;

    public BizPermCatalogException(String machineCode, String message) {
        super(message);
        this.machineCode = machineCode;
    }

    public String getMachineCode() {
        return machineCode;
    }

    public static final String NODE_NOT_FOUND = "BIZPERM_NODE_NOT_FOUND";
    public static final String NODE_HAS_CHILDREN_OR_OPS = "BIZPERM_NODE_HAS_CHILDREN_OR_OPS";
    public static final String PERMISSION_KEY_NOT_FOUND = "BIZPERM_PERMISSION_KEY_NOT_FOUND";
    public static final String OP_POINT_NOT_FOUND = "BIZPERM_OP_POINT_NOT_FOUND";
    public static final String REORDER_IDS_MISMATCH = "BIZPERM_REORDER_IDS_MISMATCH";
}
