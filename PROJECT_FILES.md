# DFBS 项目文件清单（自动生成）

生成时间：2026-01-19 11:08:52

## 常用索引（自动生成，按分组）
> 这一段用于让 ChatGPT / 自己快速定位关键文件，不需要手工维护。

### 本地入口脚本（START/END）
- DFBS-START.bat
- DFBS-END.bat
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
- backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionRepo.java
- backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteVersionService.java
- backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteVersionController.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionEntity.java
- backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionRepo.java

### 数据库迁移（Flyway）
- backend/dfbs-app/src/main/resources/db/migration/V0001__init.sql
- backend/dfbs-app/src/main/resources/db/migration/V0002__quote_version.sql
- backend/dfbs-app/src/main/resources/db/migration/V0003__quote_version_only_one_active.sql

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
DFBS-END.bat
DFBS-GEN-PROJECT-FILES.bat
DFBS-START.bat
PROJECT_FILES.md
PROJECT_STATUS.md
README.md
backend/dfbs-app/.gitattributes
backend/dfbs-app/.gitignore
backend/dfbs-app/.mvn/wrapper/maven-wrapper.properties
backend/dfbs-app/HELP.md
backend/dfbs-app/README.md
backend/dfbs-app/dep.txt
backend/dfbs-app/logs/dfbs-app.log
backend/dfbs-app/logs/dfbs-app.log.2026-01-16.0.gz
backend/dfbs-app/mvnw
backend/dfbs-app/mvnw.cmd
backend/dfbs-app/pom.xml
backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java
backend/dfbs-app/src/main/java/com/dfbs/app/HealthController.java
backend/dfbs-app/src/main/java/com/dfbs/app/application/quote/QuoteVersionService.java
backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/QuoteVersionController.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteVersionRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionController.java
backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionEntity.java
backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionRepo.java
backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionService.java
backend/dfbs-app/src/main/resources/application.properties.bak
backend/dfbs-app/src/main/resources/application.yml
backend/dfbs-app/src/main/resources/db/migration/V0001__init.sql
backend/dfbs-app/src/main/resources/db/migration/V0002__quote_version.sql
backend/dfbs-app/src/main/resources/db/migration/V0003__quote_version_only_one_active.sql
backend/dfbs-app/src/test/java/com/dfbs/app/DfbsAppApplicationTests.java
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
docs/DECISIONS.md
docs/baseline/final_01_mvp_scope_v2_1_final_full.md
docs/baseline/final_02_module_map_v2_1_final_full.mmd
docs/baseline/final_03_project_structure_v2_1_final_full.txt
gen_project_files.py
infra/docker-compose.yml
```
