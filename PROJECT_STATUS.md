# DFBS 项目状态（v2.1_final 冻结基准）

生成/维护原则：
- 只记录“已封板的结果”，不记录过程
- 一次只推进一个工程决策；封板后不回头改
- 不允许修改 docs/baseline/（v2.1_final 冻结基准）【README.md 已声明】
- 协作约定：面向纯新手输出，必须给“可照抄的完整步骤”（改动点=文件路径+整段可复制内容+验收命令+通过标准），不要求读者理解代码与术语。
## 协作与输出约定（对 ChatGPT 的硬性约束）【已封板】

- 使用对象：纯新手（不具备代码/框架/数据库/HTTP 等专业背景）
- 输出要求（必须全部满足）：
  - 必须给出“可照抄执行”的明确步骤
  - 每一步必须说明：
    - 做什么
    - 改哪个文件（完整路径）
    - 需要粘贴的**完整内容**
    - 执行什么命令
    - 看到什么结果才算通过
  - 不输出伪代码、不说“你自己理解/按需调整/自行实现”
  - 不假设读者理解任何专业术语或代码含义
- 约束级别：
  - 本约定与 v2.1_final 同级
  - 后续所有工程推进必须遵守
  - 若违反，本次输出视为无效，需要重给
---

## 0. 项目定位
- 项目：DFBS 单公司主体内部云管系统（非多租户）
- 架构：模块化单体（platform / modules(domain) / application / interfaces）
- 当前唯一权威冻结基准：v2.1_final（docs/baseline/）

## 1. 本地开发环境（已落地）
- OS：Windows 11 + Docker Desktop + WSL2
- 后端：Java 21 / Spring Boot 3.x / Maven（mvnw）
- 基础设施（Docker）：PostgreSQL 16 / Redis / RabbitMQ / MinIO
- 数据库迁移：Flyway
- 健康检查：GET http://localhost:8080/api/healthz -> ok

## 2. 本地入口脚本（已封板）
- 入口（项目根目录）：
  - DFBS-START.bat：开始点一下（git pull + infra up + backend + healthz + test）
  - DFBS-END.bat：结束点一下（git add/commit/push；无改动则提示 No changes to commit）
- 约束：入口脚本使用英文文件名，避免中文路径/编码导致 bat 调用失败
- 健康检查：GET http://localhost:8080/api/healthz -> ok
- 测试日志：backend/dfbs-app/target/dfbs-test.log

## 3. 已封板的工程决策（关键）
### 3.1 报价版本唯一生效（数据库级约束）✅ 封板
- 目标：同一 quote_no 任意时刻只能有一个 active
- 落地位置：
  - Flyway：backend/dfbs-app/src/main/resources/db/migration/V0003__quote_version_only_one_active.sql
  - PostgreSQL：对 quote_version 建立部分唯一索引（仅 active 行唯一）
- 验证结果：同一 quote_no 查询仅 1 条 is_active=true

### 3.2 报价版本激活并发保护（接口级稳定性）✅ 封板
- 接口：POST /api/quote-versions/activate
- 落地位置：
  - Controller：backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionController.java
  - Repo：backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionRepo.java
- 策略：
  - 同一 quoteNo 激活请求串行（事务内锁）
  - 先 deactivate 全部 active，再将目标版本更新为 active（不新增记录）
- 验收结果：
  - 同一 quote_no 多次 activate 后，版本记录数不变
  - 任意时刻仅 1 条 is_active=true；active_at 更新，created_at 不变
### 3.3 报价模块分层收敛（activate 下沉 Service，Controller 仅 HTTP 入口）✅ 封板
- 目标：固定事务边界与并发语义的位置，避免后续模块复制 Controller 逻辑导致返工
- 范围：仅 quote-versions activate；不改业务、不改 SQL、不改 Flyway
- 落地位置：
  - Controller：backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionController.java
  - Service：backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionService.java
  - Repo 不变：backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionRepo.java
- 验收结果（数据库）：
  - quote_no='Q001' 记录数恒定不增长
  - 任意时刻仅 1 条 is_active=true；active_at 更新，created_at 不变
### 3.4 报价 activate 自动验收测试（防回归）✅ 封板
- 目标：把“同一 quote_no 任意时刻仅 1 条 is_active=true”的封板规则变成自动化验收，防止后续改坏
- 范围：仅测试与只读统计方法；不改业务逻辑、不改 Flyway、不改表结构
- 落地位置：
  - 测试：backend/dfbs-app/src/test/java/com/dfbs/app/quote/QuoteVersionActivateTest.java
  - Repo（只读统计）：backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionRepo.java
- 验收命令：
  - cd backend/dfbs-app
  - .\mvnw test
- 验收标准：
  - BUILD SUCCESS
  - QuoteVersionActivateTest 通过（active 计数恒为 1）
### 3.5 一键验收测试脚本（双击跑 mvnw test）✅ 封板
- 目标：把后端自动验收（mvnw test）变成双击可执行，降低手工操作成本与误操作
- 落地位置：项目根目录 DFBS-验收测试.bat
- 行为：
  - 自动切到 backend/dfbs-app
  - 执行 mvnw test
  - 输出 PASS/FAIL
  - 将完整日志写入 backend/dfbs-app/target/dfbs-test.log 便于排查
- 验收方式：双击 DFBS-验收测试.bat，看到 ✅ TESTS PASS
### 3.6 报价版本 activate 测试自洽（不依赖本地数据）✅ 封板
- 目标：确保报价 activate 的自动化测试不依赖数据库中已有数据，任意环境/任意时间均可重复通过
- 策略：
  - 测试内生成独立 quote_no（UUID）
  - 测试内直接插入 2 个版本数据
  - 反复调用 activate
  - 断言数据库中任意时刻仅 1 条 is_active=true
- 落地位置：
  - 测试：backend/dfbs-app/src/test/java/com/dfbs/app/quote/QuoteVersionActivateTest.java
  - Repo（仅测试插入）：backend/dfbs-app/src/main/java/com/dfbs/app/quote/QuoteVersionRepo.java
- 验收方式：
  - 双击 DFBS-验收测试.bat
  - 或执行：cd backend/dfbs-app && .\mvnw test
- 验收标准：BUILD SUCCESS
### 3.7 脚本入口收敛为 2 个（START/END）✅ 封板
- 目标：跨电脑切换时只保留两个可执行入口，减少误用与编码问题；实现“开始点一下、结束点一下”
- 入口脚本（项目根目录）：
  - DFBS-START.bat：git pull + docker compose up -d + 启动后端(新窗口) + healthz + mvnw test（输出到 backend/dfbs-app/target/dfbs-test.log）
  - DFBS-END.bat：git add . + git commit + git push（无改动时提示 No changes to commit）
- 约束：
  - 入口脚本使用英文文件名，避免中文路径/编码导致 bat 调用失败
- 验收标准：
  - 双击 DFBS-START.bat 最终输出 ALL DONE: START OK 且 TESTS PASS
  - 双击 DFBS-END.bat 最终输出 ALL DONE: END OK 且远端 main 更新或显示 up-to-date
### 3.8 跨电脑路径/盘符不一致（C盘/D盘）允许 ✅ 封板
- 结论：允许不同电脑将 dfbs 项目放在不同磁盘与路径（例如 C:\dfbs / D:\dfbs），不影响开发与验收。
- 前提：
  - 代码以 Git 仓库为唯一一致性来源（main 分支同步）。
  - 所有本地脚本使用相对路径（%~dp0）定位项目根目录，不写死绝对路径。
- 验收方式：
  - 在任意路径的项目根目录双击 DFBS-START.bat，可完成：git pull + infra up + backend + healthz + test。
  - 双击 DFBS-END.bat，可完成：git add/commit/push（无改动时提示 No changes to commit）。
- 禁止事项：
  - 不允许脚本写死盘符/绝对路径（如 C:\dfbs\...）。
  - 不允许手动拷贝文件夹作为同步方式（以 Git 为准）。


## 4. 当前工程状态
- 后端可启动并稳定运行
- Flyway 可正常执行
- 当前已实现/验证的业务主线动作：
  - 报价版本生效（activate）

### 报价版本激活语义（版本由编辑产生，activate 只切状态）✅ 封板
- 版本唯一标识：(quote_no, version_no)（表内唯一索引）
- activate 行为：不新增 quote_version 记录；只更新既有版本的 is_active/active_at
- 验收：同一 quote_no 多次 activate 后，记录数不变；且任意时刻仅 1 条 is_active=true
- 验收（数据库）：
  - select count(*) where quote_no='Q001' 结果恒定（版本数不随 activate 增长）
  - is_active=true 永远只有 1 条；active_at 会更新，created_at 不变
