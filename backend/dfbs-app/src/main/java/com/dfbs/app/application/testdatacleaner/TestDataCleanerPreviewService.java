package com.dfbs.app.application.testdatacleaner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Read-only preview for Test Data Cleaner: estimated row counts by module and safety flag.
 * Source: evidence/tdclean/TDCLEAN_260210_001_03a_EVIDENCE.md (business tables only; must-keep excluded).
 */
@Service
public class TestDataCleanerPreviewService {

    private static final Set<String> ALL_SELECTABLE_MODULE_IDS = Set.of(
            "dashboard", "quotes", "shipments", "after-sales", "work-orders", "finance",
            "warehouse-inventory", "warehouse-replenish", "import-center", "customers", "contracts",
            "machines", "machine-models", "model-part-lists", "spare-parts", "sim-cards",
            "platform-orgs", "platform-applications", "sim-applications", "confirmation-center",
            "platform-config", "org-levels", "org-tree", "org-change-logs"
    );

    private static final Set<String> FOUNDATIONAL_MODULE_IDS = Set.of(
            "org-levels", "org-tree", "org-change-logs", "platform-config", "platform-orgs", "platform-applications"
    );

    /** moduleId -> business test data tables only (must-keep baseline excluded). */
    private static final Map<String, List<String>> MODULE_TABLES = Map.ofEntries(
            Map.entry("dashboard", List.of()),
            Map.entry("quotes", List.of("quote", "quote_item", "quote_version", "quote_workflow_history",
                    "quote_payment", "quote_collector_history", "quote_void_request", "quote_void_application")),
            Map.entry("shipments", List.of("shipment", "shipment_machine")),
            Map.entry("after-sales", List.of("after_sales")),
            Map.entry("work-orders", List.of("work_order", "work_order_record", "work_order_part")),
            Map.entry("finance", List.of("payment", "payment_allocation", "account_statement", "account_statement_item",
                    "invoice_application", "invoice_record", "invoice_item_ref")),
            Map.entry("warehouse-inventory", List.of("warehouse", "inventory", "inventory_log", "transfer_order",
                    "special_outbound_request", "wh_warehouse", "wh_inventory", "wh_stock_record")),
            Map.entry("warehouse-replenish", List.of("wh_replenish_request")),
            Map.entry("import-center", List.of()),
            Map.entry("customers", List.of("md_customer", "md_customer_alias", "md_customer_merge_log")),
            Map.entry("contracts", List.of("contracts")),
            Map.entry("machines", List.of("machines", "machine_ownership_log")),
            Map.entry("machine-models", List.of("machine_models")),
            Map.entry("model-part-lists", List.of("model_part_lists", "bom_conflicts")),
            Map.entry("spare-parts", List.of("spare_parts")),
            Map.entry("sim-cards", List.of("sim_cards", "sim_binding_log")),
            Map.entry("platform-orgs", List.of("platform_org", "platform_org_customers")),
            Map.entry("platform-applications", List.of("platform_account_applications")),
            Map.entry("sim-applications", List.of("sim_cards", "sim_binding_log")),
            Map.entry("confirmation-center", List.of()),
            Map.entry("platform-config", List.of()),
            Map.entry("org-levels", List.of()),
            Map.entry("org-tree", List.of("org_node", "org_person", "person_affiliation", "job_level",
                    "org_position_enabled", "org_position_binding", "org_level_position_template")),
            Map.entry("org-change-logs", List.of("org_change_log"))
    );

    private final JdbcTemplate jdbcTemplate;

    public TestDataCleanerPreviewService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Preview counts by module (read-only). Returns invalidModuleIds when request contains unknown module ids.
     */
    @Transactional(readOnly = true)
    public TestDataCleanerPreviewResult preview(List<String> moduleIds) {
        List<String> invalid = moduleIds == null ? List.of()
                : moduleIds.stream().filter(id -> !ALL_SELECTABLE_MODULE_IDS.contains(id)).distinct().toList();
        List<String> validIds = moduleIds == null ? List.of()
                : moduleIds.stream().filter(ALL_SELECTABLE_MODULE_IDS::contains).distinct().toList();

        List<ModuleCountItem> items = new ArrayList<>();
        long totalCount = 0;
        for (String moduleId : validIds) {
            long count = countForModule(moduleId);
            items.add(new ModuleCountItem(moduleId, count));
            totalCount += count;
        }

        List<String> reasons = new ArrayList<>();
        if (validIds.size() == ALL_SELECTABLE_MODULE_IDS.size() && new HashSet<>(validIds).equals(ALL_SELECTABLE_MODULE_IDS)) {
            reasons.add("FULL_RESET");
        }
        for (String id : validIds) {
            if (FOUNDATIONAL_MODULE_IDS.contains(id)) {
                reasons.add("FOUNDATIONAL_SELECTED:" + id);
            }
        }
        boolean requiresResetConfirm = !reasons.isEmpty();

        return new TestDataCleanerPreviewResult(
                items,
                totalCount,
                requiresResetConfirm,
                reasons,
                invalid.isEmpty() ? null : invalid
        );
    }

    private long countForModule(String moduleId) {
        List<String> tables = MODULE_TABLES.getOrDefault(moduleId, List.of());
        long sum = 0;
        for (String table : tables) {
            sum += countTable(table);
        }
        return sum;
    }

    private long countTable(String tableName) {
        try {
            Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
            return n != null ? n : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public record ModuleCountItem(String moduleId, long count) {}

    public record TestDataCleanerPreviewResult(
            List<ModuleCountItem> items,
            long totalCount,
            boolean requiresResetConfirm,
            List<String> requiresResetReasons,
            List<String> invalidModuleIds
    ) {}
}
