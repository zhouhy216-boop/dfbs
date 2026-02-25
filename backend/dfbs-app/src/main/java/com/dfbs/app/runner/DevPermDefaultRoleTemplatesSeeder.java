package com.dfbs.app.runner;

import com.dfbs.app.modules.perm.PermRoleEntity;
import com.dfbs.app.modules.perm.PermRoleRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Dev-only: seeds minimal default role templates so the Roles & Permissions page has at least one
 * template to interact with on a fresh DB. Runs when dfbs.dev.seedPermDefaultRoleTemplates=true
 * and profile "prod" is NOT active. Idempotent. Does not depend on perm_module.
 */
@Component
@ConditionalOnProperty(name = "dfbs.dev.seed-perm-default-role-templates", havingValue = "true")
@Order(102)
public class DevPermDefaultRoleTemplatesSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevPermDefaultRoleTemplatesSeeder.class);

    private static final String KEY_VIEWER = "template_viewer";
    private static final String LABEL_VIEWER = "只读模板";
    private static final String KEY_EDITOR = "template_editor";
    private static final String LABEL_EDITOR = "编辑模板";

    private final PermRoleRepo roleRepo;
    private final Environment environment;

    public DevPermDefaultRoleTemplatesSeeder(PermRoleRepo roleRepo, Environment environment) {
        this.roleRepo = roleRepo;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String profiles = Arrays.toString(environment.getActiveProfiles());
        String flagValue = environment.getProperty("dfbs.dev.seed-perm-default-role-templates", "unset");
        log.info("DevPermDefaultRoleTemplatesSeeder: flag=dfbs.dev.seedPermDefaultRoleTemplates={}, activeProfiles={}", flagValue, profiles);

        if (Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase)) {
            log.info("DevPermDefaultRoleTemplatesSeeder: skipping (prod profile active)");
            return;
        }

        boolean insertedViewer = ensureRole(KEY_VIEWER, LABEL_VIEWER, true);
        boolean insertedEditor = ensureRole(KEY_EDITOR, LABEL_EDITOR, false);
        log.info("DevPermDefaultRoleTemplatesSeeder: template_viewer={}, template_editor={}",
                insertedViewer ? "inserted" : "skipped (exists)", insertedEditor ? "inserted" : "skipped (exists)");
    }

    /** @return true if role was inserted, false if already existed */
    private boolean ensureRole(String roleKey, String label, boolean enabled) {
        if (roleRepo.existsByRoleKey(roleKey)) return false;
        PermRoleEntity e = new PermRoleEntity();
        e.setRoleKey(roleKey);
        e.setLabel(label);
        e.setEnabled(enabled);
        roleRepo.save(e);
        return true;
    }
}
