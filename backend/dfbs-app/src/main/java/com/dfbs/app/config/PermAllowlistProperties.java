package com.dfbs.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * PERM module: allowlist of super-admin user keys for 角色与权限.
 * Identity key used: userId (Long), compared as string in allowlist.
 * Config: dfbs.perm.superAdminAllowlist (comma-separated, e.g. "1,2").
 */
@ConfigurationProperties(prefix = "dfbs.perm")
public class PermAllowlistProperties {

    /** Comma-separated list of userId (as string). Empty = no one allowed. */
    private String superAdminAllowlist = "";

    public String getSuperAdminAllowlist() {
        return superAdminAllowlist;
    }

    public void setSuperAdminAllowlist(String superAdminAllowlist) {
        this.superAdminAllowlist = superAdminAllowlist != null ? superAdminAllowlist : "";
    }

    /** Normalized list of trimmed non-empty keys. */
    public List<String> getSuperAdminAllowlistKeys() {
        if (superAdminAllowlist == null || superAdminAllowlist.isBlank()) {
            return Collections.emptyList();
        }
        return Stream.of(superAdminAllowlist.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
