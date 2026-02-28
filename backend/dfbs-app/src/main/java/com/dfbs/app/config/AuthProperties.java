package com.dfbs.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Auth config: default password for new accounts and legacy login (when password_hash is null).
 * In prod must be set explicitly; dev default for verification only.
 */
@ConfigurationProperties(prefix = "dfbs.auth")
public class AuthProperties {

    /** Default password for new account creation and legacy users (password_hash null). Dev/test only; prod must set explicitly. */
    private String defaultPassword = "changeme";

    public String getDefaultPassword() {
        return defaultPassword != null ? defaultPassword : "changeme";
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }
}
