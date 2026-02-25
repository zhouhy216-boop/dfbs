package com.dfbs.app.runner;

import com.dfbs.app.modules.perm.PermModuleActionEntity;
import com.dfbs.app.modules.perm.PermModuleActionRepo;
import com.dfbs.app.modules.perm.PermModuleEntity;
import com.dfbs.app.modules.perm.PermModuleRepo;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Dev-only: seeds a small demo perm module tree (platform_application + child) so Roles & Permissions UI
 * shows checkboxes for role-permission binding verification. Runs when dfbs.dev.seedPermDemoModules=true
 * and profile "prod" is NOT active. Idempotent.
 */
@Component
@ConditionalOnProperty(name = "dfbs.dev.seed-perm-demo-modules", havingValue = "true")
@Order(101)
public class DevPermDemoModulesSeeder implements ApplicationRunner {

    private static final String ROOT_KEY = "platform_application";
    private static final String ROOT_LABEL = "平台应用";
    private static final String CHILD_KEY = "platform_application.orders";
    private static final String CHILD_LABEL = "订单";
    private static final List<String> CHILD_ACTIONS = List.of("VIEW", "CREATE", "EDIT");

    private final PermModuleRepo moduleRepo;
    private final PermModuleActionRepo moduleActionRepo;
    private final Environment environment;

    public DevPermDemoModulesSeeder(PermModuleRepo moduleRepo,
                                    PermModuleActionRepo moduleActionRepo,
                                    Environment environment) {
        this.moduleRepo = moduleRepo;
        this.moduleActionRepo = moduleActionRepo;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase)) {
            return;
        }
        PermModuleEntity root = ensureModule(ROOT_KEY, ROOT_LABEL, null);
        PermModuleEntity child = ensureModule(CHILD_KEY, CHILD_LABEL, root.getId());
        for (String actionKey : CHILD_ACTIONS) {
            if (!moduleActionRepo.existsByModuleIdAndActionKey(child.getId(), actionKey)) {
                PermModuleActionEntity ma = new PermModuleActionEntity();
                ma.setModuleId(child.getId());
                ma.setActionKey(actionKey);
                moduleActionRepo.save(ma);
            }
        }
    }

    private PermModuleEntity ensureModule(String moduleKey, String label, Long parentId) {
        return moduleRepo.findByModuleKey(moduleKey).orElseGet(() -> {
            PermModuleEntity e = new PermModuleEntity();
            e.setModuleKey(moduleKey);
            e.setLabel(label);
            e.setParentId(parentId);
            return moduleRepo.save(e);
        });
    }
}
