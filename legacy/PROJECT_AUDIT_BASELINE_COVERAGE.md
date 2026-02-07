# Baseline Coverage (vs final_01)

**Source of Truth**: `docs/baseline/final_01_mvp_scope_v2_1_final_full.md`.  
**Code/Test Evidence**: `backend/dfbs-app/src/main/java`, `src/test/java`, `PROJECT_STATUS.md` milestones.

**Definitions**:
* **DONE**: Code exists and at least one test passes (verified capability).
* **PARTIAL**: Code exists but incomplete or no/insufficient test coverage.
* **TODO**: No code found for the feature.

---

## B1. 本窗口确认“V1 做”（Appendix B1）

| Feature ID/Name | Status | Evidence (Test/API/Milestone ID) |
| :--- | :--- | :--- |
| 对账单/对账批次（Statement/Reconciliation） | DONE | AccountStatementTest, AccountStatementControllerTest; AccountStatementController, StatementReconcileController; 2026-01-30-01 |
| 回款记录（Payment/Receipt）与核销分摊（含跨周期） | DONE | PaymentTest, QuoteBatchPaymentTest, QuotePaymentWorkflowTest; PaymentController (quote + standalone), bind-payments; 2026-01-30-12/13 |
| 费用分摊/拆分（Allocation/Split） | DONE | PaymentTest (allocation); PaymentAllocationEntity, PaymentService; 2026-01-30-13 |
| 价格/费率表（PriceBook/RatePlan），含“同客户多合同按最低执行” | DONE | PriceBookTest; ContractPriceController, ContractPriceService.calculateSuggestedPrice; 2026-01-30-14 |
| 工单/服务请求（非维修） | DONE | WorkOrderQuoteTest; WorkOrderController, WorkOrderEntity; 2026-01-26-02 |
| 数据更正单（Correction） | TODO | No Correction entity or controller found |
| 承运商/快递商管理 + 运费口径（客户承担/公司成本） | DONE | CarrierTest, FreightBillTest; CarrierController, FreightBillController; ShipmentType (billable = CUSTOMER_DELEGATE); 2026-01-30-15 |
| 委托/订单（履约主线对象） | DONE | Mapped to ShipmentType (SALES_DELEGATE, PRODUCTION_DELEGATE, CUSTOMER_DELEGATE) in Shipment module; ShipmentPanoramaTest, ShipmentProcessTest; 2026-01-29-03 |
| RMA（退货/返修/换货） | PARTIAL | Repair record (返修) exists; no dedicated RMA/退货/换货 entity or flow |
| 经办人事实统计页（非绩效考核） | PARTIAL | ExpenseStatsController export; no dedicated “经办人事实统计” page/API |

---

## Section 1. 平台底座（V1 必做）

| Feature | Status | Evidence |
| :--- | :--- | :--- |
| 1.1 配置中心（Config Center） | TODO | No config versioning / publish / rollback module |
| 1.2 规则引擎（Rule Engine） | PARTIAL | AttachmentRuleService (mandatory attachment); no generic rule engine (user/record.xxx, visibility, workflow) |
| 1.3 多维度状态机（Workflow） | PARTIAL | Quote workflow (submit/audit/assign-collector); Trip/Permission/Invoice flows; no generic multi-dimension state machine |
| 1.4 权限模型（两层） | PARTIAL | ROLE-based; CurrentUserIdResolver, isFinanceOrAdmin; no rule-layer expression engine |
| 1.5 版本化与审计 | PARTIAL | Quote version; createdAt/audit fields; no generic history table / decision log |
| 1.6 异步底座与任务中心 | TODO | No MQ / task center / DLQ in codebase |
| 1.7 Webhook 与外部集成 | TODO | No webhook / external integration module |
| 1.8 搜索（一步到位） | TODO | No ES/OpenSearch; repo keyword search only |
| 1.9 附件 | PARTIAL | AttachmentRuleService (validation); URL storage; no versioning / download audit in codebase |
| 1.10 备份恢复与沙箱 | TODO | Not in backend scope |
| 1.11 多币种与汇率 | PARTIAL | Currency enum, exchange in services; no automatic rate fetch / lock-on-confirm in codebase |

---

## Section 2. 业务模块（V1 必须跑通的闭环）

| Feature | Status | Evidence |
| :--- | :--- | :--- |
| 2.1 主数据（客户/合同/机型/机器/ICCID） | DONE | Customer, Contract, Product, Machine, ICCID entities + CRUD + list/search; CustomerMergeTest, MasterDataListTest; 2026-01-22-01, 2026-01-23-04, 2026-01-30-04/05 |
| 2.1.1 合同与价格条款 | DONE | ContractPriceHeader/Item, ContractPriceController, PriceBookTest; 2026-01-30-14 |
| 2.2 发货（Shipment） | DONE | ShipmentController, ShipmentService; accept/ship/complete; ShipmentProcessTest, ShipmentPanoramaTest |
| 2.3 客户委托 / 物流委托收费来源 | DONE | ShipmentType (CUSTOMER_DELEGATE, SALES_DELEGATE, PRODUCTION_DELEGATE); Quote from shipment; ShipmentTypeService.isBillable |
| 2.4 货损记录（Damage） | DONE | DamageController, DamageRecordTest; damage_record, config types/treatments |
| 2.5 售后维修台账（Repair Ledger） | DONE | RepairRecordController, RepairRecordService, RepairRecordTest; repair_record |
| 2.6 零部件主数据与 BOM | DONE | PartEntity, ProductBomEntity, PartController, BomController; QuotePartLinkTest, BomServiceTest; 2026-01-30-02/03 |
| 2.7 报价单（Quote） | DONE | QuoteController, QuoteItemController, QuoteWorkflowController, QuoteExportController, QuoteVoidController, WorkOrderQuoteController; QuoteStateTest, QuoteItemTest, QuoteExportTest, QuoteVoidTest, WorkOrderQuoteTest; 2026-01-23-05/06, 2026-01-26-01–05, 2026-01-27-01/02, 2026-01-28-01–05 |
| 2.8 仓库管理（Inventory） | DONE | InventoryController, TransferController, OutboundController; InventoryTest; warehouse, inventory, transfer_order, special_outbound_request |
| 2.9+ 费用/报销/出差/开票/通知/权限/对账/回款 | DONE | Per module capabilities and tests; milestones 2026-01-28–30 |

---

## Summary Counts

| Status | B1 Items | Section 1 (底座) | Section 2 (业务) |
| :--- | :--- | :--- | :--- |
| DONE | 7 | 0 | 9+ |
| PARTIAL | 2 | 5 | 0 |
| TODO | 1 | 5 | 0 |
