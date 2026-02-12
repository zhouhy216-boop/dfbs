# TDCLEAN-260210-001 Step-04 Impact Check

**Request ID:** TDCLEAN-260210-001-04-IMP01  
**Related:** Step-04 (Execute cleanup) + existing preview API/UI (03.b / 03.c)

---

## 1) Likely impacted areas (facts only)

**Frontend (pages/flows):**
- **测试数据清理器** modal: opened from BasicLayout header (menu entry “测试数据清理器”); no route. Run/report will appear in this modal (currently: 预览 enabled, 执行 button disabled).
- No other frontend guard for Test Data Cleaner; entry is layout-level.

**Backend (modules/files):**
- **Controller:** `interfaces/testdatacleaner/TestDataCleanerAdminController.java` — `@RequestMapping("/api/v1/admin/test-data-cleaner")`; today only `POST /preview`; Step-04 will add execute endpoint(s) here (same base path, same `SuperAdminGuard.requireSuperAdmin()` pattern).
- **Service:** `application/testdatacleaner/TestDataCleanerPreviewService.java` — read-only preview, module→tables map, no delete. Step-04 will add or extend a service that performs DELETE/TRUNCATE by module (same module→tables source: `evidence/tdclean/TDCLEAN_260210_001_03a_EVIDENCE.md`).
- **Security:** `config/SuperAdminGuard.java` — already used by controller; execute must reuse same gating (SUPER_ADMIN).

**APIs/contracts:**
- **Existing:** `POST /api/v1/admin/test-data-cleaner/preview` — request: `{ moduleIds, includeAttachments? }`; response: `items`, `totalCount`, `requiresResetConfirm`, `requiresResetReasons`, `invalidModuleIds?`.
- **Step-04:** New endpoint(s) on same base path (e.g. `POST .../execute` or `POST .../run`) — same `moduleIds` (and likely `confirmText` when `requiresResetConfirm`); response may include per-module or global delete counts/report. Preview remains read-only and unchanged.

---

## 2) Regression watchlist (must-test suggestions)

- **Login / top navigation (Mode A):** After cleanup, confirm login still works and top menu (including “测试数据清理器”) and core navigation render; must-keep tables (e.g. `app_user`, `permission_request`) are not touched by cleaner.
- **Critical pages that may hard-fail if baseline tables are touched:** Platform config (depends on `md_platform`), org-level config (`org_level`), org tree / positions (`org_position_catalog`), quote dictionary/config (e.g. `payment_method`, `fee_category`, `fee_type`), shipment/carrier config (`md_carrier`, `md_carrier_rule`), warehouse/config (`warehouse_config`), business line (`business_line`). Ensure none of these are in the delete list (evidence 03a: must-keep list).
- **Authorization:** Execute endpoint must be gated to SUPER_ADMIN only (reuse `SuperAdminGuard`); non–super-admin must receive 403.
- **Safety:** Do not touch RabbitMQ by default (no RabbitMQ usage found in backend `src` today). Redis: if Step-04 includes cache clear, define and document when it runs (e.g. after DB cleanup); avoid clearing cache before DB is consistent.

---

## 3) Current build/test status (facts)

| Check | Result | Notes |
|-------|--------|------|
| Backend `mvnw compile -DskipTests` | PASS | — |
| Backend `mvnw test` | FAIL | 220 tests, 13 failures, 7 errors. Failing/error test names: ExpenseStatsTest.customerGrouping_viaWorkOrder_groupsUnderCust1; MasterDataListTest.iccidList_boundCustomerNameAndPlanPlatformExpiry; DictionaryQuoteTest.scenario5_unitAutoFix_onConfirm, scenario2_confirmBlock_freeTextFails; QuotePaymentWorkflowTest.scenario1_happyPath_partialThenFullPayment; QuoteDownstreamTest.test5_permission_nonInitiator_triesCreate_fail; QuoteStandardizationTest.fullFlow_lenientEntryStrictExit_standardizeThenConfirm, confirmBlock_partNotStandardized_throwsQuoteValidationException; ArchitectureRulesTest.interfaces_must_not_be_accessed_by_application_or_modules; ContractMasterDataSearchTest.testSoftDeleteExcluded; MachineMasterDataCreateTest.can_create_machine; OrgChangeLogControllerTest (list_onlyFrom_returns200, list_withObjectId_returns200, list_fromAndTo_returns200, list_onlyTo_returns200, list_noFilters_returns200AndJsonPage, list_withObjectType_returns200); OrgNodeControllerTest.getNodeById_returns200_andJsonWithScalarFieldsOnly; MasterDataReadOnlyRulesTest.interfaces_must_not_depend_on_any_repo, only_masterdata_application_packages_may_depend_on_masterdata_repos. |
| Frontend `npm run build` | FAIL | Existing TS errors (tsc -b); e.g. OrgTreeSelect, BasicLayout, Admin/OrgTree, AfterSales, ImportCenter, MasterData, Platform/Application, Platform/Org, Quote, Shipment, System/PlatformConfig, WorkOrder/Internal/Detail. |

---

*Facts only; no refactor proposals; no scope expansion.*
