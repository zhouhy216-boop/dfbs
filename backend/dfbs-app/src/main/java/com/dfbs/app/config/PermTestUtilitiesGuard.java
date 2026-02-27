package com.dfbs.app.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Test-only gate: test utilities (e.g. Role-Vision) must be 404 when not in strict test env.
 * Requires: dfbs.perm.testUtilitiesEnabled=true AND active profiles do NOT include "prod".
 */
@Component
public class PermTestUtilitiesGuard {

    private final PermAllowlistProperties allowlistProperties;
    private final Environment environment;

    public PermTestUtilitiesGuard(PermAllowlistProperties allowlistProperties, Environment environment) {
        this.allowlistProperties = allowlistProperties;
        this.environment = environment;
    }

    /** Throws 404 if test utilities are disabled (prod profile or flag false). */
    public void requireTestUtilitiesEnabled() {
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase)) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        if (!allowlistProperties.isTestUtilitiesEnabled()) {
            throw new ResponseStatusException(NOT_FOUND);
        }
    }
}
