# DFBS 项目文件清单（自动生成）

生成时间：2026-01-27 20:28:24

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
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dto/WorkOrderImportRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/payment/PaymentMethodService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/payment/QuotePaymentService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/void/QuoteVoidService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteExportController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteItemController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteVersionController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/WorkOrderQuoteController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dictionary/DictionaryController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/CreateItemRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/CreateQuoteRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/QuoteItemDto.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/QuoteResponseDto.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/UpdateItemRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/UpdateQuoteRequest.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/payment/PaymentController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/payment/PaymentMethodController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/void/QuoteVoidController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteItemEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteItemRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteSequenceEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteSequenceRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeCategoryEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeCategoryRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeTypeEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeTypeRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/Currency.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/PaymentStatus.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteExpenseType.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteItemWarehouse.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuotePaymentStatus.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteSourceType.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteStatus.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteVoidStatus.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/PaymentMethodEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/PaymentMethodRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuoteCollectorHistoryEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuoteCollectorHistoryRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuotePaymentEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuotePaymentRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidApplicationEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidApplicationRepo.java

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
backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java
backend/dfbs-app/src/main/java/com/dfbs/app/HealthController.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/contract/ContractMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/customer/CustomerMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/iccid/IccidMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/machine/MachineMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/masterdata/PartBomService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/notification/NotificationService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/product/ProductMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteExportService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteItemService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteNumberService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteValidationException.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteVersionService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dictionary/FeeDictionaryService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dictionary/QuoteItemValidationHelper.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/dto/WorkOrderImportRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/payment/PaymentMethodService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/payment/QuotePaymentService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/void/QuoteVoidService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/settings/BusinessLineService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/settings/WarehouseConfigService.java
backend/dfbs-app/src/main/java/com/dfbs/app/config/CompanyInfoProperties.java
backend/dfbs-app/src/main/java/com/dfbs/app/config/CurrentUserProvider.java
backend/dfbs-app/src/main/java/com/dfbs/app/config/JpaAuditingConfig.java
backend/dfbs-app/src/main/java/com/dfbs/app/config/UserInfoProvider.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/contract/ContractDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/contract/ContractMasterDataController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/customer/CustomerDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/customer/CustomerMasterDataController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/iccid/IccidMasterDataController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/machine/MachineMasterDataController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/masterdata/PartController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/notification/NotificationController.java
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
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/QuoteResponseDto.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/UpdateItemRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/dto/UpdateQuoteRequest.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/payment/PaymentController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/payment/PaymentMethodController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/void/QuoteVoidController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/settings/BusinessLineController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/settings/WarehouseConfigController.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/contract/ContractEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/contract/ContractRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/customer/CustomerEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/customer/CustomerRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/iccid/IccidEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/iccid/IccidRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/machine/MachineEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/machine/MachineRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/PartEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/PartRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/ProductBomEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/masterdata/ProductBomRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/notification/NotificationEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/notification/NotificationRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/product/ProductEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/product/ProductRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteItemEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteItemRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteSequenceEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteSequenceRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeCategoryEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeCategoryRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeTypeEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/dictionary/FeeTypeRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/Currency.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/PaymentStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteExpenseType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteItemWarehouse.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuotePaymentStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteSourceType.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/enums/QuoteVoidStatus.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/PaymentMethodEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/PaymentMethodRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuoteCollectorHistoryEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuoteCollectorHistoryRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuotePaymentEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/payment/QuotePaymentRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidApplicationEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/void/QuoteVoidApplicationRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/settings/BusinessLineEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/settings/BusinessLineRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/settings/WarehouseConfigEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/settings/WarehouseConfigRepo.java
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
backend/dfbs-app/src/main/resources/templates/excel/quote_template_v3.xlsx
backend/dfbs-app/src/test/java/com/dfbs/app/ArchitectureRulesTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/DfbsAppApplicationTests.java
backend/dfbs-app/src/test/java/com/dfbs/app/MasterDataReadOnlyRulesTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteCcTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteExportTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteItemTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteNumberingTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteNumberingTestConfig.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteStateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/QuoteWarehouseTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/TestClock.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/WorkOrderQuoteTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/dictionary/DictionaryLogicTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/dictionary/DictionaryQuoteTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/payment/PaymentRecordTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/application/quote/void_/QuoteVoidTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/contract/ContractMasterDataCreateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/contract/ContractMasterDataSearchTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/customer/CustomerMasterDataCreateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/customer/CustomerMasterDataSearchTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/iccid/IccidMasterDataCreateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/machine/MachineMasterDataCreateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/product/ProductMasterDataCreateTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/product/ProductMasterDataSearchTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/quote/QuoteVersionActivateTest.java
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
gen_project_files.py
infra/docker-compose.yml
tools/new_module.py
```
