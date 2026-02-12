package com.dfbs.app.application.testdatacleaner;

import com.dfbs.app.config.RedisFlusher;
import com.dfbs.app.interfaces.testdatacleaner.dto.TestDataCleanerExecuteResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Best-effort cleanup execution for Test Data Cleaner. Uses same module→tables and safety
 * rules as preview; deletes in FK-safe order (children first per 03a evidence). Clears Redis when available.
 */
@Service
public class TestDataCleanerExecuteService {

    /** Delete order: first element = delete first (child). Whitelist-only. */
    private static final Map<String, List<String>> MODULE_TABLES_DELETE_ORDER = Map.ofEntries(
            Map.entry("dashboard", List.of()),
            Map.entry("quotes", List.of("quote_item", "quote_payment", "quote_workflow_history",
                    "quote_void_request", "quote_void_application", "quote_collector_history", "quote_version", "quote")),
            Map.entry("shipments", List.of("shipment_machine", "shipment")),
            Map.entry("after-sales", List.of("after_sales")),
            Map.entry("work-orders", List.of("work_order_record", "work_order_part", "work_order")),
            Map.entry("finance", List.of("invoice_item_ref", "invoice_record", "invoice_application",
                    "payment_allocation", "payment", "account_statement_item", "account_statement")),
            Map.entry("warehouse-inventory", List.of("inventory_log", "transfer_order", "special_outbound_request",
                    "wh_stock_record", "wh_inventory", "wh_warehouse", "inventory", "warehouse")),
            Map.entry("warehouse-replenish", List.of("wh_replenish_request")),
            Map.entry("import-center", List.of()),
            Map.entry("customers", List.of("md_customer_alias", "md_customer_merge_log", "md_customer")),
            Map.entry("contracts", List.of("contracts")),
            Map.entry("machines", List.of("machine_ownership_log", "machines")),
            Map.entry("machine-models", List.of("machine_models")),
            Map.entry("model-part-lists", List.of("bom_conflicts", "model_part_lists")),
            Map.entry("spare-parts", List.of("spare_parts")),
            Map.entry("sim-cards", List.of("sim_binding_log", "sim_cards")),
            Map.entry("platform-orgs", List.of("platform_org_customers", "platform_org")),
            Map.entry("platform-applications", List.of("platform_account_applications")),
            Map.entry("sim-applications", List.of("sim_binding_log", "sim_cards")),
            Map.entry("confirmation-center", List.of()),
            Map.entry("platform-config", List.of()),
            Map.entry("org-levels", List.of()),
            Map.entry("org-tree", List.of("org_position_enabled", "org_position_binding", "person_affiliation",
                    "job_level", "org_level_position_template", "org_person", "org_node")),
            Map.entry("org-change-logs", List.of("org_change_log"))
    );

    private final JdbcTemplate jdbcTemplate;
    private final TestDataCleanerPreviewService previewService;
    private final Optional<RedisFlusher> redisFlusher;

    public TestDataCleanerExecuteService(
            JdbcTemplate jdbcTemplate,
            TestDataCleanerPreviewService previewService,
            java.util.Optional<RedisFlusher> redisFlusher) {
        this.jdbcTemplate = jdbcTemplate;
        this.previewService = previewService;
        this.redisFlusher = redisFlusher != null ? redisFlusher : Optional.empty();
    }

    /**
     * Run cleanup for given module ids. Caller must have validated requiresResetConfirm/confirmText and includeAttachments.
     * Best-effort: each table delete commits independently (no global transaction).
     */
    public TestDataCleanerExecuteResponse execute(List<String> moduleIds) {
        String startedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        TestDataCleanerPreviewService.TestDataCleanerPreviewResult preview = previewService.preview(moduleIds);

        List<String> invalid = preview.invalidModuleIds() != null ? preview.invalidModuleIds() : List.of();
        List<String> validIds = moduleIds == null ? List.of()
                : moduleIds.stream()
                .filter(id -> !invalid.contains(id) && MODULE_TABLES_DELETE_ORDER.containsKey(id))
                .distinct().toList();

        List<TestDataCleanerExecuteResponse.ModuleExecuteItemDto> items = new ArrayList<>();
        long totalDeleted = 0;

        for (String moduleId : validIds) {
            List<String> tables = MODULE_TABLES_DELETE_ORDER.getOrDefault(moduleId, List.of());
            List<TestDataCleanerExecuteResponse.TableResultDto> tableResults = new ArrayList<>();
            long moduleDeleted = 0;
            int success = 0, failed = 0;
            for (String table : tables) {
                TestDataCleanerExecuteResponse.TableResultDto tr = deleteTable(table);
                tableResults.add(tr);
                if ("SUCCESS".equals(tr.status())) {
                    moduleDeleted += tr.deleted();
                    success++;
                } else if ("FAILED".equals(tr.status())) {
                    failed++;
                }
            }
            String moduleStatus = failed == 0 ? "SUCCESS" : (success == 0 ? "FAILED" : "PARTIAL");
            items.add(new TestDataCleanerExecuteResponse.ModuleExecuteItemDto(
                    moduleId, tableResults, moduleDeleted, moduleStatus));
            totalDeleted += moduleDeleted;
        }

        String redisMessage;
        try {
            if (redisFlusher.isPresent()) {
                redisFlusher.get().flushDb();
                redisMessage = "Redis cache cleared.";
            } else {
                redisMessage = "Redis not configured.";
            }
        } catch (Exception e) {
            redisMessage = "Redis clear failed: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }

        String finishedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        long partialOrFailed = items.stream().filter(i -> !"SUCCESS".equals(i.moduleStatus())).count();
        String status = items.isEmpty() ? "FAILED"
                : (partialOrFailed == 0 ? "SUCCESS" : (partialOrFailed == items.size() ? "FAILED" : "PARTIAL"));

        return new TestDataCleanerExecuteResponse(
                startedAt,
                finishedAt,
                preview.requiresResetConfirm(),
                preview.requiresResetReasons() != null ? preview.requiresResetReasons() : List.of(),
                invalid.isEmpty() ? List.of() : invalid,
                items,
                totalDeleted,
                status,
                redisMessage
        );
    }

    private TestDataCleanerExecuteResponse.TableResultDto deleteTable(String tableName) {
        if (!isWhitelisted(tableName)) {
            return new TestDataCleanerExecuteResponse.TableResultDto(tableName, 0, "SKIPPED", "Table not in whitelist");
        }
        try {
            int deleted = jdbcTemplate.update("DELETE FROM " + tableName);
            return new TestDataCleanerExecuteResponse.TableResultDto(tableName, deleted, "SUCCESS", null);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return new TestDataCleanerExecuteResponse.TableResultDto(tableName, 0, "FAILED", msg);
        }
    }

    private boolean isWhitelisted(String tableName) {
        return MODULE_TABLES_DELETE_ORDER.values().stream().anyMatch(list -> list.contains(tableName));
    }

}
