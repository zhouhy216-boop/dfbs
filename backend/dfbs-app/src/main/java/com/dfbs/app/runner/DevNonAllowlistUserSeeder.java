package com.dfbs.app.runner;

import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Dev-only: ensures a non-allowlist test user "admin2" exists for 403 / menu-hidden verification.
 * Runs when dfbs.dev.seedNonAllowlistUser=true and profile "prod" is NOT active (safety).
 * Idempotent: does nothing if admin2 already exists. Do NOT add admin2's id to dfbs.perm.superAdminAllowlist.
 */
@Component
@ConditionalOnProperty(name = "dfbs.dev.seed-non-allowlist-user", havingValue = "true")
@Order(100)
public class DevNonAllowlistUserSeeder implements ApplicationRunner {

    private static final String USERNAME = "admin2";
    private static final String NICKNAME = "Admin2";
    /** Same as admin seed (V0047) so login works; not in allowlist so PERM returns 403. */
    private static final String AUTHORITIES = "[\"ROLE_ADMIN\"]";

    private final UserRepo userRepo;
    private final Environment environment;

    public DevNonAllowlistUserSeeder(UserRepo userRepo, Environment environment) {
        this.userRepo = userRepo;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase)) {
            return;
        }
        if (userRepo.findByUsername(USERNAME).isPresent()) {
            return;
        }
        UserEntity user = new UserEntity();
        user.setUsername(USERNAME);
        user.setNickname(NICKNAME);
        user.setCanRequestPermission(true);
        user.setAuthorities(AUTHORITIES);
        user.setAllowNormalNotification(true);
        user.setCanManageStatements(true);
        userRepo.save(user);
    }
}
