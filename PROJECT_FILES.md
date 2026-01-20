# DFBS 项目文件清单（自动生成）

生成时间：2026-01-20 16:28:52

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
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteVersionService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteVersionController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionRepo.java

### 数据库迁移（Flyway）
- backend/dfbs-app/src/main/resources/db/migration/V0001__init.sql
- backend/dfbs-app/src/main/resources/db/migration/V0002__quote_version.sql
- backend/dfbs-app/src/main/resources/db/migration/V0003__quote_version_only_one_active.sql
- backend/dfbs-app/src/main/resources/db/migration/V0004__masterdata_init.sql

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
PROJECT_STATUS.md
PROJECT_STATUS_OLD.md
README.md
backend/dfbs-app/.gitattributes
backend/dfbs-app/.gitignore
backend/dfbs-app/.mvn/wrapper/maven-wrapper.properties
backend/dfbs-app/HELP.md
backend/dfbs-app/README.md
backend/dfbs-app/dep.txt
backend/dfbs-app/logs/dfbs-app.log
backend/dfbs-app/logs/dfbs-app.log.2026-01-16.0.gz
backend/dfbs-app/logs/dfbs-app.log.2026-01-19.0.gz
backend/dfbs-app/mvnw
backend/dfbs-app/mvnw.cmd
backend/dfbs-app/pom.xml
backend/dfbs-app/run.log
backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java
backend/dfbs-app/src/main/java/com/dfbs/app/HealthController.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/contract/ContractMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/customer/CustomerMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/iccid/IccidMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/machine/MachineMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/product/ProductMasterDataService.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteVersionService.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/customer/CustomerMasterDataController.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteVersionController.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/contract/ContractEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/contract/ContractRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/customer/CustomerEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/customer/CustomerRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/iccid/IccidEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/iccid/IccidRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/machine/MachineEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/machine/MachineRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/product/ProductEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/product/ProductRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionRepo.java
backend/dfbs-app/src/main/resources/application.properties.bak
backend/dfbs-app/src/main/resources/application.yml
backend/dfbs-app/src/main/resources/db/migration/V0001__init.sql
backend/dfbs-app/src/main/resources/db/migration/V0002__quote_version.sql
backend/dfbs-app/src/main/resources/db/migration/V0003__quote_version_only_one_active.sql
backend/dfbs-app/src/main/resources/db/migration/V0004__masterdata_init.sql
backend/dfbs-app/src/test/java/com/dfbs/app/ArchitectureRulesTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/DfbsAppApplicationTests.java
backend/dfbs-app/src/test/java/com/dfbs/app/MasterDataReadOnlyRulesTest.java
backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/customer/CustomerMasterDataCreateTest.java
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
logs/dfbs-app-run_20260120_105306.log
logs/dfbs-app-run_20260120_111624.log
logs/dfbs-app-run_20260120_112153.log
logs/dfbs-app-run_20260120_112915.log
logs/dfbs-app-run_20260120_113746.log
logs/dfbs-app-run_20260120_121311.log
logs/dfbs-app-run_20260120_121740.log
logs/dfbs-app-run_20260120_122118.log
logs/dfbs-app-run_20260120_122157.log
logs/dfbs-app-run_20260120_124226.log
logs/dfbs-app-run_20260120_124534.log
logs/dfbs-app-run_20260120_124620.log
logs/dfbs-app-run_20260120_124928.log
logs/dfbs-app-run_20260120_135619.log
logs/dfbs-app-run_20260120_135704.log
logs/dfbs-app-run_20260120_135948.log
logs/dfbs-end_20260120_113931.log
logs/dfbs-end_20260120_120642.log
logs/dfbs-end_20260120_121355.log
logs/dfbs-end_20260120_121727.log
logs/dfbs-end_20260120_122147.log
logs/dfbs-end_20260120_124855.log
logs/dfbs-end_20260120_125247.log
logs/dfbs-end_20260120_135127.log
logs/dfbs-end_20260120_162842.log
logs/dfbs-gen-project-files_20260120_113856.log
logs/dfbs-gen-project-files_20260120_124830.log
logs/dfbs-gen-project-files_20260120_125254.log
logs/dfbs-gen-project-files_20260120_135025.log
logs/dfbs-gen-project-files_20260120_162851.log
logs/dfbs-git-pull_20260120_125302.log
logs/dfbs-healthz_20260120_111643.log
logs/dfbs-healthz_20260120_111812.log
logs/dfbs-healthz_20260120_112921.log
logs/dfbs-healthz_20260120_113751.log
logs/dfbs-healthz_20260120_121317.log
logs/dfbs-healthz_20260120_121522.log
logs/dfbs-healthz_20260120_121746.log
logs/dfbs-healthz_20260120_122124.log
logs/dfbs-healthz_20260120_122202.log
logs/dfbs-healthz_20260120_124557.log
logs/dfbs-healthz_20260120_124640.log
logs/dfbs-healthz_20260120_124943.log
logs/dfbs-healthz_20260120_142115.log
logs/dfbs-healthz_20260120_145912.log
logs/dfbs-healthz_20260120_150634.log
logs/dfbs-infra-up_20260120_104812.log
logs/dfbs-infra-up_20260120_104853.log
logs/dfbs-infra-up_20260120_105019.log
logs/dfbs-infra-up_20260120_105259.log
logs/dfbs-infra-up_20260120_111457.log
logs/dfbs-infra-up_20260120_111613.log
logs/dfbs-infra-up_20260120_112152.log
logs/dfbs-infra-up_20260120_112915.log
logs/dfbs-infra-up_20260120_113744.log
logs/dfbs-infra-up_20260120_121310.log
logs/dfbs-infra-up_20260120_121740.log
logs/dfbs-infra-up_20260120_122117.log
logs/dfbs-infra-up_20260120_122157.log
logs/dfbs-infra-up_20260120_124226.log
logs/dfbs-infra-up_20260120_124534.log
logs/dfbs-infra-up_20260120_124620.log
logs/dfbs-infra-up_20260120_124918.log
logs/dfbs-infra-up_20260120_135619.log
logs/dfbs-infra-up_20260120_135657.log
logs/dfbs-infra-up_20260120_135905.log
logs/dfbs-stop-app_20260120_124216.log
logs/dfbs-stop-app_20260120_124343.log
logs/dfbs-stop-app_20260120_124522.log
logs/dfbs-stop-app_20260120_124612.log
logs/dfbs-stop-app_20260120_124754.log
logs/dfbs-stop-app_20260120_124908.log
logs/dfbs-stop-app_20260120_125000.log
logs/dfbs-stop-app_20260120_125242.log
logs/dfbs-stop-app_20260120_135612.log
logs/dfbs-stop-app_20260120_135644.log
logs/dfbs-stop-app_20260120_135844.log
logs/dfbs-stop-app_20260120_135901.log
logs/dfbs-stop-app_20260120_162833.log
logs/dfbs-test_20260120_110415.log
logs/dfbs-test_20260120_111900.log
logs/dfbs-test_20260120_113907.log
logs/dfbs-test_20260120_124711.log
logs/dfbs-test_20260120_135737.log
logs/dfbs-test_20260120_135848.log
logs/dfbs-test_20260120_135914.log
logs/dfbs-test_20260120_141100.log
logs/dfbs-test_20260120_141713.log
logs/dfbs-test_20260120_141826.log
logs/dfbs-test_20260120_142051.log
logs/dfbs-test_20260120_142803.log
logs/dfbs-test_20260120_143254.log
logs/dfbs-test_20260120_144817.log
logs/dfbs-test_20260120_145848.log
logs/dfbs-test_20260120_150607.log
logs/dfbs-test_20260120_150707.log
logs/dfbs-test_20260120_150913.log
logs/dfbs-test_20260120_151205.log
logs/dfbs-test_20260120_151322.log
logs/dfbs-test_20260120_151434.log
logs/dfbs-test_20260120_151536.log
logs/dfbs-test_20260120_151853.log
logs/dfbs-test_20260120_152022.log
logs/dfbs-test_20260120_152050.log
logs/dfbs-test_20260120_153519.log
tools/new_module.py
```
