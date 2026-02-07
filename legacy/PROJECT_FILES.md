# DFBS 项目文件清单（自动生成）

生成时间：2026-02-04 13:52:35

## 常用索引（自动生成，按分组）
> 这一段用于让 ChatGPT / 自己快速定位关键文件，不需要手工维护。

### 本地入口脚本（START/END）
- gen_project_files.py

### 基础设施（Docker）
- infra/docker-compose.yml

### 权威冻结基准（v2.1_final）
- README.md
- PROJECT_STATUS.md
- docs/baseline/final_01_mvp_scope_v2_1_final_full.md
- docs/baseline/final_02_module_map_v2_1_final_full.mmd
- docs/baseline/final_03_project_structure_v2_1_final_full.txt
- docs/DECISIONS.md

### 后端入口与健康检查
- backend/dfbs-app/pom.xml
- backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java
- backend/dfbs-app/src/main/java/com/dfbs/app/HealthController.java
- backend/dfbs-app/src/main/resources/application.yml

### 报价模块（当前关注）
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteExportService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteItemService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteNumberService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteValidationException.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteVersionService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dictionary/FeeDictionaryService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dictionary/QuoteItemValidationHelper.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dto/BatchPaymentRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dto/QuoteFilterRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dto/QuotePendingPaymentDTO.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dto/WorkOrderImportRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/payment/PaymentMethodService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/payment/QuotePaymentService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/void/QuoteVoidService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/workflow/QuoteWorkflowService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteExportController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteItemController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteVersionController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/WorkOrderQuoteController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dictionary/DictionaryController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/CreateItemRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/CreateQuoteRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/QuoteItemDto.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/QuoteListDto.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/QuoteResponseDto.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/UpdateItemRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/UpdateQuoteRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/payment/PaymentController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/payment/PaymentMethodController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/void/QuoteVoidController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/workflow/QuoteWorkflowController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteItemEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteItemRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteSequenceEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteSequenceRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteSpecification.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeCategoryEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeCategoryRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeTypeEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeTypeRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/Currency.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/DownstreamType.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/PaymentStatus.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteExpenseType.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteInvoiceStatus.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteItemWarehouse.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuotePaymentStatus.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteSourceType.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteStatus.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteVoidStatus.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/WorkflowAction.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/PaymentMethodEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/PaymentMethodRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuoteCollectorHistoryEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuoteCollectorHistoryRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuotePaymentEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuotePaymentRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidApplicationEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidApplicationRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidRequestEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidRequestRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/VoidRequesterRole.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/VoidRequestStage.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/VoidRequestStatus.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/workflow/QuoteWorkflowHistoryEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/workflow/QuoteWorkflowHistoryRepo.java

### 数据库迁移（Flyway）
- backend/dfbs-app/src/main/resources/db/migration/V0001__init.sql
- backend/dfbs-app/src/main/resources/db/migration/V0002__quote_version.sql
- backend/dfbs-app/src/main/resources/db/migration/V0003__quote_version_only_one_active.sql
- backend/dfbs-app/src/main/resources/db/migration/V0004__masterdata_init.sql
- backend/dfbs-app/src/main/resources/db/migration/V0005__quote_item.sql
- backend/dfbs-app/src/main/resources/db/migration/V0006__quote_header_and_sequence.sql
- backend/dfbs-app/src/main/resources/db/migration/V0007__quote_item_mvp.sql
- backend/dfbs-app/src/main/resources/db/migration/V0008__quote_workorder_fields.sql
- backend/dfbs-app/src/main/resources/db/migration/V0009__fee_dictionary_and_bom.sql
- backend/dfbs-app/src/main/resources/db/migration/V0010__payment_record.sql
- backend/dfbs-app/src/main/resources/db/migration/V0011__quote_void_process.sql
- backend/dfbs-app/src/main/resources/db/migration/V0012__quote_cc_leadership.sql
- backend/dfbs-app/src/main/resources/db/migration/V0013__quote_cc_warehouse.sql
- backend/dfbs-app/src/main/resources/db/migration/V0014__quote_payment_workflow.sql
- backend/dfbs-app/src/main/resources/db/migration/V0015__invoice_workflow.sql
- backend/dfbs-app/src/main/resources/db/migration/V0016__quote_void_process.sql
- backend/dfbs-app/src/main/resources/db/migration/V0017__quote_downstream.sql
- backend/dfbs-app/src/main/resources/db/migration/V0018__shipment_workflow.sql
- backend/dfbs-app/src/main/resources/db/migration/V0019__shipment_panorama.sql
- backend/dfbs-app/src/main/resources/db/migration/V0020__damage_record.sql
- backend/dfbs-app/src/main/resources/db/migration/V0021__freight_bill.sql
- backend/dfbs-app/src/main/resources/db/migration/V0022__inventory_mvp.sql
- backend/dfbs-app/src/main/resources/db/migration/V0023__repair_ledger.sql
- backend/dfbs-app/src/main/resources/db/migration/V0024__permission_request.sql
- backend/dfbs-app/src/main/resources/db/migration/V0025__notification_center.sql
- backend/dfbs-app/src/main/resources/db/migration/V0026__account_statement.sql
- backend/dfbs-app/src/main/resources/db/migration/V0027__bom_versioning.sql
- backend/dfbs-app/src/main/resources/db/migration/V0028__quote_part_link.sql
- backend/dfbs-app/src/main/resources/db/migration/V0029__machine_iccid_list_support.sql
- backend/dfbs-app/src/main/resources/db/migration/V0030__quote_standardization.sql
- backend/dfbs-app/src/main/resources/db/migration/V0031__customer_merge_support.sql
- backend/dfbs-app/src/main/resources/db/migration/V0032__expense_claim_mvp.sql
- backend/dfbs-app/src/main/resources/db/migration/V0033__trip_request_mvp.sql
- backend/dfbs-app/src/main/resources/db/migration/V0034__payment_allocation_mvp.sql
- backend/dfbs-app/src/main/resources/db/migration/V0035__contract_pricing_mvp.sql
- backend/dfbs-app/src/main/resources/db/migration/V0036__carrier_freight_bill_mvp.sql
- backend/dfbs-app/src/main/resources/db/migration/V0037__correction_mvp.sql
- backend/dfbs-app/src/main/resources/db/migration/V0038__after_sales_mvp.sql
- backend/dfbs-app/src/main/resources/db/migration/V0039__masterdata_six_pack.sql
- backend/dfbs-app/src/main/resources/db/migration/V0040__bom_conflicts_and_spare_part_aliases.sql
- backend/dfbs-app/src/main/resources/db/migration/V0041__quote_payment_created_at.sql
- backend/dfbs-app/src/main/resources/db/migration/V0042__warehouse_mvp.sql
- backend/dfbs-app/src/main/resources/db/migration/V0043__work_order_service_flow.sql
- backend/dfbs-app/src/main/resources/db/migration/V0044__work_order_cancellation_reason.sql
- backend/dfbs-app/src/main/resources/db/migration/V0045__work_order_refinement.sql
- backend/dfbs-app/src/main/resources/db/migration/V0046__app_user_username.sql
- backend/dfbs-app/src/main/resources/db/migration/V0047__app_user_seed_admin.sql
- backend/dfbs-app/src/main/resources/db/migration/V0048__smart_select_infrastructure.sql
- backend/dfbs-app/src/main/resources/db/migration/V0049__platform_org_master.sql
- backend/dfbs-app/src/main/resources/db/migration/V0050__platform_account_applications.sql
- backend/dfbs-app/src/main/resources/db/migration/V0051__refactor_platform_org.sql

### 后端工程骨架（分层目录）
- backend/pom.xml
- backend/dfbs-platform/.gitkeep
- backend/dfbs-platform/README.md
- backend/dfbs-modules/.gitkeep
- backend/dfbs-modules/README.md
- backend/dfbs-application/.gitkeep
- backend/dfbs-application/README.md
- backend/dfbs-interfaces/.gitkeep
- backend/dfbs-interfaces/README.md

## 全量文件列表（自动生成）

```
.cursorrules
.gitignore
DFBS-APP-RUN.ps1
DFBS-DEV.ps1
DFBS-END.ps1
DFBS-GEN-PROJECT-FILES.ps1
DFBS-GIT-PULL.ps1
DFBS-HEALTHZ.ps1
DFBS-INFRA-UP.ps1
DFBS-LOG-CLEAN.ps1
DFBS-NEW-MODULE.ps1
DFBS-STOP-APP.ps1
DFBS-TEST.ps1
PROJECT_AUDIT_BASELINE_COVERAGE.md
PROJECT_AUDIT_MODULE_CAPABILITIES.md
PROJECT_AUDIT_NEXT_STEPS.md
PROJECT_FILES.md
PROJECT_PARKING_LOT.md
PROJECT_STATUS.md
README.md
backend/dfbs-app/.gitattributes
backend/dfbs-app/.gitignore
backend/dfbs-app/.mvn/wrapper/maven-wrapper.properties
backend/dfbs-app/HELP.md
backend/dfbs-app/README.md
backend/dfbs-app/dep.txt
backend/dfbs-app/mvnw
backend/dfbs-app/mvnw.cmd
backend/dfbs-app/pom.xml
backend/dfbs-app/scripts/wipe-schema.sql
backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java
backend/dfbs-app/src/main/java/com/dfbs/app/HealthController.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/aftersales/AfterSalesService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/attachment/AttachmentPoint.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/attachment/AttachmentRuleService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/attachment/AttachmentTargetType.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/attachment/AttachmentType.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/attachment/AttachmentUploadService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/bom/BomImportExcelRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/bom/BomService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/carrier/CarrierService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/contractprice/ContractPriceService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/contractprice/PriceSuggestionDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/correction/CorrectionExecutor.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/correction/CorrectionExecutorFactory.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/correction/CorrectionService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/correction/ExpenseCorrectionExecutor.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/correction/FreightBillCorrectionExecutor.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/correction/PaymentCorrectionExecutor.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/correction/QuoteCorrectionExecutor.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/customer/CustomerMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/customer/CustomerMergeService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/customer/dto/CustomerMergeResponse.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/damage/CompensationConfirmRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/damage/DamageCreateRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/damage/DamageRecordDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/damage/DamageService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/damage/RepairStageUpdateRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/ClaimService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/ExchangeRateService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/ExpenseExportService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/ExpenseService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/ExpenseStatsDetailRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/ExpenseStatsItemDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/ExpenseStatsRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/ExpenseStatsService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/ExpenseStatsSummaryRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/GroupBy.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/expense/dto/ExpenseDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/freightbill/FreightBillDetailRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/freightbill/FreightBillService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/freightbill/FreightBillSummaryRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/freightbill/ItemUpdateDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/iccid/IccidMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/iccid/dto/IccidListDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/ContractImportRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/ContractImportService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/CustomerImportRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/CustomerImportService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/ImportServiceDelegate.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/MachineImportRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/MachineImportService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/MachineModelImportRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/MachineModelImportService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/ModelPartListImportRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/ModelPartListImportService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/SimCardImportRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/SimCardImportService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/SparePartImportRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/SparePartImportService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/dto/ImportActionReq.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/dto/ImportConflictDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/dto/ImportFailureDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/importdata/dto/ImportResultDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/inventory/InventoryService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/inventory/OutboundService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/inventory/SpecialOutboundService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/inventory/TransferService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/inventory/WarehouseSelectionResult.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/inventory/WarehouseSelectionService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/invoice/InvoiceApplicationService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/invoice/dto/InvoiceApplicationCreateRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/invoice/dto/InvoiceGroupRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/invoice/dto/QuoteItemSelection.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/machine/dto/MachineListDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/ContractMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/MachineMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/MachineModelMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/ModelPartListBomService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/ModelPartListMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/PartBomService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/PartImportExcelRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/PartImportResult.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/SimCardMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/SparePartMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/dto/BomItemDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/dto/CreateDraftRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/dto/CreateDraftResponse.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/notification/NotificationService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/payment/PaymentService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/permission/ApproveRequestDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/permission/PermissionRequestDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/permission/PermissionRequestService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformaccount/PlatformAccountApplicationService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformaccount/dto/OrgMatchCheckResult.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformaccount/dto/PlatformAccountApplicationCreateRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformaccount/dto/PlatformAccountApplicationResponse.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformaccount/dto/PlatformAccountApproveRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformaccount/dto/PlatformAccountPlannerSubmitRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformaccount/dto/PlatformAccountRejectRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformorg/PlatformOrgService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformorg/PlatformOrgValidator.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformorg/dto/PlatformOrgRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/platformorg/dto/PlatformOrgResponse.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/product/ProductMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteExportService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteItemService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteNumberService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteValidationException.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteVersionService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dictionary/FeeDictionaryService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dictionary/QuoteItemValidationHelper.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dto/BatchPaymentRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dto/QuoteFilterRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dto/QuotePendingPaymentDTO.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dto/WorkOrderImportRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/payment/PaymentMethodService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/payment/QuotePaymentService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/void/QuoteVoidService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/workflow/QuoteWorkflowService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/repair/ImportResult.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/repair/RepairRecordExcelRow.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/repair/RepairRecordFilterRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/repair/RepairRecordService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/settings/BusinessLineService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/settings/WarehouseConfigService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/EntrustShipmentCreateRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/MachineEntryDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/NormalShipmentCreateRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/ParsedShipmentDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/ReasonRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/ShipActionRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/ShipmentCreateRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/ShipmentFilterRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/ShipmentListDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/ShipmentService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/ShipmentTypeService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/shipment/SimpleShipmentCreateRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/smartselect/ConfirmationService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/smartselect/SmartSelectService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/smartselect/TempDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/smartselect/dto/GetOrCreateTempRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/smartselect/dto/GetOrCreateTempResult.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/smartselect/dto/SmartSelectItemDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/smartselect/dto/TempPoolConfirmRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/smartselect/dto/TempPoolItemDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/statement/AccountStatementService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/statement/StatementReconcileService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/triprequest/TripRequestService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/warehouse/WarehouseDataInitializer.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/warehouse/WhCoreService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/warehouse/WhReplenishService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/warehouse/dto/WhApproveReq.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/warehouse/dto/WhInboundReq.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/warehouse/dto/WhOutboundReq.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/warehouse/dto/WhReplenishReq.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/workorder/WorkOrderService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/workorder/dto/WorkOrderAcceptReq.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/workorder/dto/WorkOrderCreateReq.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/workorder/dto/WorkOrderDetailDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/workorder/dto/WorkOrderDispatchReq.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/workorder/dto/WorkOrderListDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/workorder/dto/WorkOrderPartReq.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/workorder/dto/WorkOrderRecordReq.java
backend/dfbs-app/src/main/java/com/dfbs/app/config/CompanyInfoProperties.java
backend/dfbs-app/src/main/java/com/dfbs/app/config/CurrentUserIdResolver.java
backend/dfbs-app/src/main/java/com/dfbs/app/config/CurrentUserProvider.java
backend/dfbs-app/src/main/java/com/dfbs/app/config/JpaAuditingConfig.java
backend/dfbs-app/src/main/java/com/dfbs/app/config/SecurityConfig.java
backend/dfbs-app/src/main/java/com/dfbs/app/config/UserInfoProvider.java
backend/dfbs-app/src/main/java/com/dfbs/app/infra/config/GlobalExceptionHandler.java
backend/dfbs-app/src/main/java/com/dfbs/app/infra/config/OpenApiConfig.java
backend/dfbs-app/src/main/java/com/dfbs/app/infra/dto/ErrorResult.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/aftersales/AfterSalesController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/attachment/AttachmentController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/auth/AuthController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/bom/BomController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/carrier/CarrierController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/contractprice/ContractPriceController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/correction/CorrectionController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/customer/CustomerDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/customer/CustomerMasterDataController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/customer/CustomerMergeRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/damage/DamageController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/expense/ClaimController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/expense/ClaimDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/expense/CreateClaimRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/expense/CreateExpenseRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/expense/ExpenseController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/expense/ExpenseStatsController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/expense/UpdateExpenseRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/freightbill/FreightBillController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/iccid/IccidMasterDataController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/importdata/ImportController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/inventory/InventoryController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/inventory/OutboundController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/inventory/TransferController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/invoice/InvoiceApplicationController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/masterdata/ContractController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/masterdata/DisableRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/masterdata/MachineController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/masterdata/MachineModelController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/masterdata/ModelPartListController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/masterdata/PartController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/masterdata/SimCardController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/masterdata/SparePartController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/notification/NotificationController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/payment/BindPaymentsRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/payment/CreatePaymentRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/payment/PaymentController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/permission/PermissionRequestController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/platformaccount/PlatformAccountApplicationController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/platformorg/PlatformOrgController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/product/ProductDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/product/ProductMasterDataController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteExportController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteItemController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteVersionController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/WorkOrderQuoteController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dictionary/DictionaryController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/CreateItemRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/CreateQuoteRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/QuoteItemDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/QuoteListDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/QuoteResponseDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/UpdateItemRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/UpdateQuoteRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/payment/PaymentController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/payment/PaymentMethodController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/void/QuoteVoidController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/workflow/QuoteWorkflowController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/repair/RepairRecordController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/settings/BusinessLineController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/settings/WarehouseConfigController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/shipment/ShipmentController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/smartselect/ConfirmationController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/smartselect/SmartSelectController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/statement/AccountStatementController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/statement/StatementReconcileController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/triprequest/CancelRequestRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/triprequest/CreateTripRequestRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/triprequest/TripRequestController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/triprequest/TripRequestDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/triprequest/UpdateTripRequestRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/warehouse/WhReplenishController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/warehouse/WhStockController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/workorder/WorkOrderController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/workorder/WorkOrderPublicController.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/aftersales/AfterSalesEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/aftersales/AfterSalesRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/aftersales/AfterSalesStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/aftersales/AfterSalesType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/bom/BomItemEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/bom/BomItemRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/bom/BomVersionEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/bom/BomVersionRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/carrier/CarrierEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/carrier/CarrierRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/carrier/CarrierRuleEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/carrier/CarrierRuleRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/contractprice/ContractPriceHeaderEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/contractprice/ContractPriceHeaderRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/contractprice/ContractPriceItemEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/contractprice/ContractPriceItemRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/contractprice/ContractStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/correction/CorrectionEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/correction/CorrectionRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/correction/CorrectionStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/correction/CorrectionTargetType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/customer/CustomerAliasEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/customer/CustomerAliasRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/customer/CustomerEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/customer/CustomerMergeLogEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/customer/CustomerMergeLogRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/customer/CustomerRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/damage/CompensationStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/damage/DamageRecordEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/damage/DamageRecordRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/damage/RepairStage.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/damage/TreatmentBehavior.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/damage/config/DamageTreatmentEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/damage/config/DamageTreatmentRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/damage/config/DamageTypeEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/damage/config/DamageTypeRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/expense/ClaimEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/expense/ClaimRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/expense/ClaimStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/expense/ExpenseEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/expense/ExpenseRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/expense/ExpenseStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/expense/ExpenseType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/freightbill/FinancialCategory.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/freightbill/FreightBillEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/freightbill/FreightBillItemEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/freightbill/FreightBillItemRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/freightbill/FreightBillRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/freightbill/FreightBillStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/iccid/IccidEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/iccid/IccidRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/InventoryEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/InventoryLogEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/InventoryLogRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/InventoryRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/SpecialOutboundRequestEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/SpecialOutboundRequestRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/SpecialOutboundStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/SpecialOutboundType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/TransactionType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/TransferOrderEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/TransferOrderRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/TransferStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/WarehouseEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/WarehouseRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/inventory/WarehouseType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/invoice/InvoiceApplicationEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/invoice/InvoiceApplicationRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/invoice/InvoiceApplicationStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/invoice/InvoiceItemRefEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/invoice/InvoiceItemRefRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/invoice/InvoiceRecordEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/invoice/InvoiceRecordRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/invoice/InvoiceType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/BaseMasterEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/BomConflictEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/BomConflictRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/BomConflictStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/BomConflictType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/BomStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/ContractEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/ContractRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/MachineEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/MachineModelEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/MachineModelRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/MachineOwnershipLogEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/MachineOwnershipLogRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/MachineRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/MasterDataStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/ModelPartListEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/ModelPartListRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/PartEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/PartRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/ProductBomEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/ProductBomRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/ResolutionType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/SimBindingLogEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/SimBindingLogRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/SimCardEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/SimCardRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/SparePartEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/SparePartRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/notification/NotificationEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/notification/NotificationPriority.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/notification/NotificationRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/notification/NotificationType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/payment/PaymentAllocationEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/payment/PaymentAllocationRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/payment/PaymentEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/payment/PaymentRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/payment/PaymentStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/permission/PermissionRequestEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/permission/PermissionRequestRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/permission/RequestStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/platformaccount/PlatformAccountApplicationEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/platformaccount/PlatformAccountApplicationRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/platformaccount/PlatformAccountApplicationStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/platformorg/PlatformOrgCustomerEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/platformorg/PlatformOrgCustomerId.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/platformorg/PlatformOrgCustomerRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/platformorg/PlatformOrgEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/platformorg/PlatformOrgPlatform.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/platformorg/PlatformOrgRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/product/ProductEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/product/ProductRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteItemEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteItemRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteSequenceEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteSequenceRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteSpecification.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeCategoryEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeCategoryRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeTypeEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeTypeRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/Currency.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/DownstreamType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/PaymentStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteExpenseType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteInvoiceStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteItemWarehouse.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuotePaymentStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteSourceType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteVoidStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/WorkflowAction.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/PaymentMethodEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/PaymentMethodRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuoteCollectorHistoryEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuoteCollectorHistoryRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuotePaymentEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuotePaymentRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidApplicationEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidApplicationRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidRequestEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidRequestRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/VoidRequestStage.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/VoidRequestStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/VoidRequesterRole.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/workflow/QuoteWorkflowHistoryEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/workflow/QuoteWorkflowHistoryRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/repair/RepairRecordEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/repair/RepairRecordRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/repair/RepairRecordSpecification.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/repair/RepairSource.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/repair/WarrantyStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/settings/BusinessLineEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/settings/BusinessLineRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/settings/WarehouseConfigEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/settings/WarehouseConfigRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/shipment/ApprovalStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/shipment/PackagingType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/shipment/ShipmentEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/shipment/ShipmentMachineEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/shipment/ShipmentMachineRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/shipment/ShipmentRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/shipment/ShipmentStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/shipment/ShipmentType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/statement/AccountStatementEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/statement/AccountStatementItemEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/statement/AccountStatementItemRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/statement/AccountStatementRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/statement/StatementStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/triprequest/TripRequestEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/triprequest/TripRequestRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/triprequest/TripRequestStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/user/UserEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/user/UserRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/BaseAuditEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/OutboundRefType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/ReplenishStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/StockRecordType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/WarehouseType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/WhInventoryEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/WhInventoryRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/WhReplenishRequestEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/WhReplenishRequestRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/WhStockRecordEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/WhStockRecordRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/WhWarehouseEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/warehouse/WhWarehouseRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/workorder/BaseAuditEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/workorder/PartUsageStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/workorder/WorkOrderEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/workorder/WorkOrderPartEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/workorder/WorkOrderPartRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/workorder/WorkOrderRecordEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/workorder/WorkOrderRecordRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/workorder/WorkOrderRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/workorder/WorkOrderStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/workorder/WorkOrderType.java
backend/dfbs-app/src/main/resources/application.properties.bak
backend/dfbs-app/src/main/resources/application.yml
backend/dfbs-app/src/main/resources/db/migration/V0001__init.sql
backend/dfbs-app/src/main/resources/db/migration/V0002__quote_version.sql
backend/dfbs-app/src/main/resources/db/migration/V0003__quote_version_only_one_active.sql
backend/dfbs-app/src/main/resources/db/migration/V0004__masterdata_init.sql
backend/dfbs-app/src/main/resources/db/migration/V0005__quote_item.sql
backend/dfbs-app/src/main/resources/db/migration/V0006__quote_header_and_sequence.sql
backend/dfbs-app/src/main/resources/db/migration/V0007__quote_item_mvp.sql
backend/dfbs-app/src/main/resources/db/migration/V0008__quote_workorder_fields.sql
backend/dfbs-app/src/main/resources/db/migration/V0009__fee_dictionary_and_bom.sql
backend/dfbs-app/src/main/resources/db/migration/V0010__payment_record.sql
backend/dfbs-app/src/main/resources/db/migration/V0011__quote_void_process.sql
backend/dfbs-app/src/main/resources/db/migration/V0012__quote_cc_leadership.sql
backend/dfbs-app/src/main/resources/db/migration/V0013__quote_cc_warehouse.sql
backend/dfbs-app/src/main/resources/db/migration/V0014__quote_payment_workflow.sql
backend/dfbs-app/src/main/resources/db/migration/V0015__invoice_workflow.sql
backend/dfbs-app/src/main/resources/db/migration/V0016__quote_void_process.sql
backend/dfbs-app/src/main/resources/db/migration/V0017__quote_downstream.sql
backend/dfbs-app/src/main/resources/db/migration/V0018__shipment_workflow.sql
backend/dfbs-app/src/main/resources/db/migration/V0019__shipment_panorama.sql
backend/dfbs-app/src/main/resources/db/migration/V0020__damage_record.sql
backend/dfbs-app/src/main/resources/db/migration/V0021__freight_bill.sql
backend/dfbs-app/src/main/resources/db/migration/V0022__inventory_mvp.sql
backend/dfbs-app/src/main/resources/db/migration/V0023__repair_ledger.sql
backend/dfbs-app/src/main/resources/db/migration/V0024__permission_request.sql
backend/dfbs-app/src/main/resources/db/migration/V0025__notification_center.sql
backend/dfbs-app/src/main/resources/db/migration/V0026__account_statement.sql
backend/dfbs-app/src/main/resources/db/migration/V0027__bom_versioning.sql
backend/dfbs-app/src/main/resources/db/migration/V0028__quote_part_link.sql
backend/dfbs-app/src/main/resources/db/migration/V0029__machine_iccid_list_support.sql
backend/dfbs-app/src/main/resources/db/migration/V0030__quote_standardization.sql
backend/dfbs-app/src/main/resources/db/migration/V0031__customer_merge_support.sql
backend/dfbs-app/src/main/resources/db/migration/V0032__expense_claim_mvp.sql
backend/dfbs-app/src/main/resources/db/migration/V0033__trip_request_mvp.sql
backend/dfbs-app/src/main/resources/db/migration/V0034__payment_allocation_mvp.sql
backend/dfbs-app/src/main/resources/db/migration/V0035__contract_pricing_mvp.sql
backend/dfbs-app/src/main/resources/db/migration/V0036__carrier_freight_bill_mvp.sql
backend/dfbs-app/src/main/resources/db/migration/V0037__correction_mvp.sql
backend/dfbs-app/src/main/resources/db/migration/V0038__after_sales_mvp.sql
backend/dfbs-app/src/main/resources/db/migration/V0039__masterdata_six_pack.sql
backend/dfbs-app/src/main/resources/db/migration/V0040__bom_conflicts_and_spare_part_aliases.sql
backend/dfbs-app/src/main/resources/db/migration/V0041__quote_payment_created_at.sql
backend/dfbs-app/src/main/resources/db/migration/V0042__warehouse_mvp.sql
backend/dfbs-app/src/main/resources/db/migration/V0043__work_order_service_flow.sql
backend/dfbs-app/src/main/resources/db/migration/V0044__work_order_cancellation_reason.sql
backend/dfbs-app/src/main/resources/db/migration/V0045__work_order_refinement.sql
backend/dfbs-app/src/main/resources/db/migration/V0046__app_user_username.sql
backend/dfbs-app/src/main/resources/db/migration/V0047__app_user_seed_admin.sql
backend/dfbs-app/src/main/resources/db/migration/V0048__smart_select_infrastructure.sql
backend/dfbs-app/src/main/resources/db/migration/V0049__platform_org_master.sql
backend/dfbs-app/src/main/resources/db/migration/V0050__platform_account_applications.sql
backend/dfbs-app/src/main/resources/db/migration/V0051__refactor_platform_org.sql
backend/dfbs-app/src/main/resources/templates/excel/quote_template_v3.xlsx
backend/dfbs-app/src/test/java/com/dfbs/app/ArchitectureRulesTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/DfbsAppApplicationTests.java
backend/dfbs-app/src/test/java/com/dfbs/app/MasterDataReadOnlyRulesTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/attachment/AttachmentRuleTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/bom/BomServiceTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/carrier/CarrierTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/contractprice/PriceBookTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/correction/CorrectionTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/customer/CustomerMergeTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/damage/DamageRecordTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/expense/ExpenseClaimTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/expense/ExpenseStatsTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/freightbill/FreightBillTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/inventory/InventoryTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/invoice/InvoiceApplicationTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/masterdata/MasterDataListTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/notification/NotificationTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/payment/PaymentTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/permission/PermissionRequestTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteCcTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteDownstreamTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteExportTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteItemTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteNumberingTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteNumberingTestConfig.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuotePartLinkTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteStandardizationTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteStateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteWarehouseTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/TestClock.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/WorkOrderQuoteTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/dictionary/DictionaryLogicTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/dictionary/DictionaryQuoteTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/payment/PaymentRecordTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/payment/QuoteBatchPaymentTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/payment/QuotePaymentWorkflowTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/void_/QuoteVoidTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/repair/RepairRecordTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/shipment/ShipmentPanoramaTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/shipment/ShipmentProcessTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/statement/AccountStatementTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/triprequest/TripRequestTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/config/ForceFlywayCleanConfig.java
backend/dfbs-app/src/test/java/com/dfbs/app/infra/SwaggerTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/contract/ContractMasterDataCreateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/contract/ContractMasterDataSearchTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/contractprice/ContractPriceControllerTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/customer/CustomerMasterDataCreateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/customer/CustomerMasterDataSearchTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/iccid/IccidMasterDataCreateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/machine/MachineMasterDataCreateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/product/ProductMasterDataCreateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/product/ProductMasterDataSearchTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/quote/QuoteVersionActivateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/statement/AccountStatementControllerTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/scripts/WipeSchemaMain.java
backend/dfbs-app/src/test/resources/application.properties
backend/dfbs-application/.gitkeep
backend/dfbs-application/README.md
backend/dfbs-interfaces/.gitkeep
backend/dfbs-interfaces/README.md
backend/dfbs-modules/.gitkeep
backend/dfbs-modules/README.md
backend/dfbs-platform/.gitkeep
backend/dfbs-platform/README.md
backend/pom.xml
deprecated-bat/DFBS-END.bat
deprecated-bat/DFBS-GEN-PROJECT-FILES.bat
deprecated-bat/DFBS-NEW-MODULE.bat
deprecated-bat/DFBS-START.bat
deprecated-bat/DFBS-TEST.bat
docs/DECISIONS.md
docs/baseline/final_01_mvp_scope_v2_1_final_full.md
docs/baseline/final_02_module_map_v2_1_final_full.mmd
docs/baseline/final_03_project_structure_v2_1_final_full.txt
frontend/dfbs-ui/README.md
frontend/dfbs-ui/index.html
frontend/dfbs-ui/package-lock.json
frontend/dfbs-ui/package.json
frontend/dfbs-ui/src/App.tsx
frontend/dfbs-ui/src/components/Access.tsx
frontend/dfbs-ui/src/components/AttachmentList.tsx
frontend/dfbs-ui/src/components/SmartReferenceSelect/index.tsx
frontend/dfbs-ui/src/index.css
frontend/dfbs-ui/src/layouts/BasicLayout.tsx
frontend/dfbs-ui/src/main.tsx
frontend/dfbs-ui/src/pages/Admin/ConfirmationCenter/index.tsx
frontend/dfbs-ui/src/pages/AfterSales/Detail.tsx
frontend/dfbs-ui/src/pages/AfterSales/index.tsx
frontend/dfbs-ui/src/pages/Customer/index.tsx
frontend/dfbs-ui/src/pages/Dashboard/index.tsx
frontend/dfbs-ui/src/pages/Finance/index.tsx
frontend/dfbs-ui/src/pages/ImportCenter/index.tsx
frontend/dfbs-ui/src/pages/Login/index.tsx
frontend/dfbs-ui/src/pages/MasterData/Contract/index.tsx
frontend/dfbs-ui/src/pages/MasterData/Machine/Detail.tsx
frontend/dfbs-ui/src/pages/MasterData/Machine/index.tsx
frontend/dfbs-ui/src/pages/MasterData/MachineModel/Detail.tsx
frontend/dfbs-ui/src/pages/MasterData/MachineModel/components/ConflictDrawer.tsx
frontend/dfbs-ui/src/pages/MasterData/MachineModel/index.tsx
frontend/dfbs-ui/src/pages/MasterData/ModelPartList/index.tsx
frontend/dfbs-ui/src/pages/MasterData/SimCard/Detail.tsx
frontend/dfbs-ui/src/pages/MasterData/SimCard/index.tsx
frontend/dfbs-ui/src/pages/MasterData/SparePart/index.tsx
frontend/dfbs-ui/src/pages/Platform/Application/index.tsx
frontend/dfbs-ui/src/pages/Platform/Org/index.tsx
frontend/dfbs-ui/src/pages/Quote/index.tsx
frontend/dfbs-ui/src/pages/Shipment/index.tsx
frontend/dfbs-ui/src/pages/Warehouse/Inventory/index.tsx
frontend/dfbs-ui/src/pages/Warehouse/Replenish/index.tsx
frontend/dfbs-ui/src/pages/WorkOrder/Internal/Detail.tsx
frontend/dfbs-ui/src/pages/WorkOrder/Internal/index.tsx
frontend/dfbs-ui/src/pages/WorkOrder/Public/index.tsx
frontend/dfbs-ui/src/stores/useAuthStore.ts
frontend/dfbs-ui/src/utils/adapters.ts
frontend/dfbs-ui/src/utils/request.ts
frontend/dfbs-ui/src/vite-env.d.ts
frontend/dfbs-ui/tsconfig.app.json
frontend/dfbs-ui/tsconfig.json
frontend/dfbs-ui/vite.config.ts
gen_project_files.py
infra/docker-compose.yml
tools/new_module.py
```
