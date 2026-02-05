package com.dfbs.app.modules.platformorg;

/**
 * Lifecycle status of a platform org. DELETED triggers renaming org_code_short to free the unique index.
 */
public enum PlatformOrgStatus {
    ACTIVE("启用"),
    ARREARS("欠费"),
    DELETED("已删除");

    private final String label;

    PlatformOrgStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
