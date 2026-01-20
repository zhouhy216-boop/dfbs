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
### 3.9 本地自动化脚本入口收敛（START/END）✅ 封板
- 目标：跨电脑使用时只保留“开始点一下 / 结束点一下”两个入口，减少误用与编码问题。
- 入口脚本（项目根目录）：
  - DFBS-START.bat：git pull + docker compose up -d + 启动后端(新窗口) + healthz + mvnw test（日志：backend/dfbs-app/target/dfbs-test.log）
  - DFBS-END.bat：git add . + git commit -m "sync" + git push（无改动时提示 No changes to commit）
- 约束：入口脚本使用英文文件名；脚本使用相对路径（%~dp0），不依赖盘符/绝对路径。
- 验收标准：
  - 双击 DFBS-START.bat 最终输出：ALL DONE: START OK 且 TESTS PASS
  - 双击 DFBS-END.bat 最终输出：ALL DONE: END OK 且远端 main 更新或 up-to-date
- 说明：3.7 与 3.9 内容同属“入口脚本收敛（START/END）”，3.9 为最终表述；保留 3.7 作为历史记录。

### 3.10 PROJECT_FILES.md 自动生成机制（gen_project_files）✅ 封板
- 目标：PROJECT_FILES.md 由脚本自动生成，作为“文件路径/结构”的唯一准据；禁止手工维护该文件内容。
- 生成方式（项目根目录）：
  - 运行：DFBS-GEN-PROJECT-FILES.bat
  - 产物：PROJECT_FILES.md（写入时间/大小可在脚本输出中确认）
- 约束：
  - gen_project_files.py 为生成器本体；DFBS-GEN-PROJECT-FILES.bat 为可视化包装器（输出写入成功 + git status）
  - 删除旧生成/启动脚本（如 dfbs_boot.py、DFBS-一键启动.bat）属于“入口收敛”的一部分
- 验收标准：
  - 运行 DFBS-GEN-PROJECT-FILES.bat 输出 OK -> PROJECT_FILES.md 且 git status 显示 PROJECT_FILES.md 变化
### 3.11 代码包路径分层落地（platform / modules / application / interfaces）【已封板】
- 完成内容：
  - Controller → interfaces
  - Service → application
  - Entity / Repository → modules
- 清理内容：
  - 删除旧 com.dfbs.app.quote.* 源码，避免 Spring Bean 冲突
- 验收结果：
  - mvnw compile / test-compile 通过
  - Spring Boot 正常启动
  - /api/healthz 返回 OK
- 风险说明：
  - 若本地修改未 push，DFBS-START.bat 的 git pull 会还原旧代码
  - 已通过提交远端解决
- 说明：3.1~3.6 中出现的 com.dfbs.app.quote.* 路径属于历史记录；自 3.11 起按分层结构迁移为 interfaces/application/modules 下的同名组件。
### 3.12 新模块标准骨架生成规则（DFBS-NEW-MODULE）✅ 封板
- 目的：新增模块不允许手工“猜放哪层”，统一生成骨架，避免后续大规模返工
- 强制分层路径：
  - interfaces：backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/<module_key>/
  - application：backend/dfbs-app/src/main/java/com/dfbs/app/application/<module_key>/
  - modules：backend/dfbs-app/src/main/java/com/dfbs/app/modules/<module_key>/
  - test：backend/dfbs-app/src/test/java/com/dfbs/app/interfaces/<module_key>/
- 工具：
  - DFBS-NEW-MODULE.bat + tools/new_module.py
- 验收：
  - 生成 demo 模块后，mvnw clean test 通过
### 3.13 Demo 模块清理策略（最干净）✅ 封板
- 原则：Demo 仅用于验证模块生成器，不作为真实业务模块保留
- 动作：
  - 删除 demo 在 interfaces / application / modules / test 下的全部目录
  - 保留 DFBS-NEW-MODULE 生成器作为唯一新增模块入口
- 验收：
  - mvnw clean test 通过
  - 项目内无 demo 相关路径
### 3.14 模块依赖边界规则（interfaces -> application -> modules -> platform）✅ 封板
- 单向依赖强制：interfaces → application → modules → platform
- 落地方式：引入 ArchUnit 测试（ArchitectureRulesTest）作为守门员，违反规则则 mvnw test 失败
- 验收：mvnw clean test 通过
### 3.15 业务模块推进顺序（地基 -> 承重 -> 上层）✅ 封板
- 冻结推进顺序（避免返工）：
  1) 主数据（customer / contract / product / machine / iccid）
  2) 报价（quote）
  3) 发货（shipment）
  4) 售后/维修/收费
  5) 平台费 / ICCID 计费
  6) 报表 / 审计 / 对外接口
- 约束：本条仅冻结“先后顺序”，不进入字段/表/API 设计

### 3.16 主数据模块拆分（边界与引用方向）✅ 封板
- 主数据拆分为 5 个模块：
  - customer / contract / product / machine / iccid
- 允许引用方向（单向）：
  - contract -> customer
  - machine -> contract
  - machine -> product
  - iccid -> machine
- 禁止：任何主数据模块双向引用、反向引用

### 3.17 主数据模块骨架生成（空骨架占坑）✅ 封板
- 使用 DFBS-NEW-MODULE 生成 5 个模块骨架（不写字段、不写业务逻辑）：
  - customer / contract / product / machine / iccid
- 验收：mvnw clean test 通过

### 3.18 主数据模块内关系冻结（字段级之前最后一道关）✅ 封板
- 职责冻结：
  - customer：客户身份与唯一编号；不包含合同/设备/ICCID；不包含平台费 org_code
  - contract：合同编号与引用 customer；不包含机器/ICCID 清单
  - product：型号定义；不包含客户/合同/ICCID
  - machine：引用 contract 与 product；客户只能通过 contract 间接关联
  - iccid：引用 machine（允许为空/解绑）；不直接引用 customer/contract/product
- 引用方式冻结：只允许用对方“业务主键”引用（不引用名称等描述字段）
- 软删除冻结：deleted_at 作为软删除标记；业务主键不复用

### 3.19 主数据最小字段集 + 首次建表（Flyway V0004）✅ 封板
- 新增迁移：V0004__masterdata_init.sql，创建 5 张主数据表：
  - md_customer / md_contract / md_product / md_machine / md_iccid
- 引用原则（3.18 冻结）：只用业务主键互相引用：
  - contract.customer_code -> customer.customer_code
  - machine.contract_no -> contract.contract_no
  - machine.product_code -> product.product_code
  - iccid.machine_sn -> machine.machine_sn（允许为空）
- 软删除：deleted_at 为空=有效；不物理删除；业务主键保持唯一避免复用
- 验收：mvnw -q clean test 通过
### 3.20 主数据 Repository 只读访问规则 ✅ 封板
- 主数据模块（customer / contract / product / machine / iccid）的 Repo：
  - 允许：查询类方法（findBy / findAll / existsBy 等）
  - 禁止：在业务模块中直接调用 save / delete
- 写入入口冻结：
  - 主数据写入只能通过专用的 MasterDataService（后续阶段实现）
  - 报价 / 发货 / 售后 / 平台费模块只能引用主数据，不得修改
- 删除策略冻结：
  - 禁止物理删除
  - 删除语义通过设置 deleted_at 实现
### 3.21 主数据只读规则自动化守门（ArchUnit）✅ 封板
- 新增测试：MasterDataReadOnlyRulesTest
- 规则：
  - interfaces(main) 层禁止依赖任何 *Repo（不检查 test 代码）
  - 除对应的 application.<module>.. 包外，禁止依赖主数据 5 个 Repo：
    customer / contract / product / machine / iccid
- 技术实现：
  - ArchUnit 仅扫描 main（ImportOption.Predefined.DO_NOT_INCLUDE_TESTS）
- 验收：mvnw clean test 通过
### 3.22 主数据“写入口”占坑（MasterDataService）✅ 封板
- 目标：为每个主数据模块建立唯一写入口（先占坑），避免未来写逻辑分散在 Repo/Controller/其他模块导致返工。
- 范围：仅新增空 Service 骨架；不引入任何业务逻辑；不新增/修改数据库结构；不修改 3.21 守门规则。
- 落地位置（application 层）：
  - backend/dfbs-app/src/main/java/com/dfbs/app/application/customer/CustomerMasterDataService.java
  - backend/dfbs-app/src/main/java/com/dfbs/app/application/contract/ContractMasterDataService.java
  - backend/dfbs-app/src/main/java/com/dfbs/app/application/product/ProductMasterDataService.java
  - backend/dfbs-app/src/main/java/com/dfbs/app/application/machine/MachineMasterDataService.java
  - backend/dfbs-app/src/main/java/com/dfbs/app/application/iccid/IccidMasterDataService.java
- 约束：
  - 主数据写入未来只能通过对应 MasterDataService 进入
  - 其他层/其他模块禁止直接写主数据 Repo（由 MasterDataReadOnlyRulesTest 守门）
- 验收：
  - cd backend/dfbs-app && .\mvnw clean test
  - BUILD SUCCESS
### 3.23 新增安全测试脚本 DFBS-TEST.bat ✅ 封板
- 背景：DFBS-START.bat 会执行 git pull / docker up / 启动后端等，存在“误触导致回到上次 END 状态”的风险。
- 决策：新增 DFBS-TEST.bat，仅运行后端单元测试，不触碰 git / docker / 启动服务，降低误操作风险。
- 文件：
  - DFBS-TEST.bat
- 验收：
  - 双击 DFBS-TEST.bat
  - 输出 [OK] TESTS PASS / BUILD SUCCESS
### 3.24 主数据 Repo 写方法调用自动化守门（加强）✅ 封板
- 背景：仅限制 Repo 依赖范围（3.21）仍可能出现 repo.save/delete 被误调用的情况。
- 决策：通过 ArchUnit 测试，限制主数据 Repo 的写方法（save/delete*）只能在 *MasterDataService 中调用。
- 规则说明：
  - 检查范围：main 代码（排除 test）
  - 允许位置：*MasterDataService
  - 禁止行为：在其他类中直接调用主数据 Repo 的 save / delete* 方法
- 文件：
  - backend/dfbs-app/src/test/java/com/dfbs/app/MasterDataReadOnlyRulesTest.java
- 验收：
  - 双击 DFBS-TEST.bat
  - 所有测试通过（BUILD SUCCESS）
### 3.25 本地自动化迁移为纯 PowerShell（ps1 + 快捷方式双击）✅ 封板
- 背景：bat 容易闪退且日志/窗口体验差；希望统一在 PowerShell 窗口运行，并避免日志被占用导致失败。
- 决策：新增 ps1 脚本作为唯一逻辑来源，通过 Windows 快捷方式（lnk）实现双击运行 PowerShell 窗口；不再依赖 bat。
- 新增文件：
  - DFBS-TEST.ps1
  - DFBS-END.ps1
  - DFBS-GEN-PROJECT-FILES.ps1
  - DFBS-NEW-MODULE.ps1
- 运行方式：双击对应快捷方式（powershell.exe -NoExit -ExecutionPolicy Bypass -File ...）
- 验收：
  - 双击 DFBS-TEST 快捷方式，测试通过，logs 下生成 dfbs-test_*.log
  - 双击 DFBS-GEN-PROJECT-FILES 快捷方式，PROJECT_FILES.md 更新包含 ps1
  - 双击 DFBS-END 快捷方式，git 提交/推送成功或显示无变更
## 3.26 本地自动化：拆分 DFBS-START 为安全的 PowerShell 脚本（封板）

### 背景与目标
- 原 DFBS-START 绑定了 git pull / infra / app run / test，存在误点导致工作区状态变化的风险。
- 目标：拆分为“单职责、安全按钮”，避免返工与误操作；所有脚本均为 PowerShell，默认不闪退，输出可追溯。

### 决策
- 不再推荐使用“一键 START”模型；以多个单职责脚本替代，避免隐式副作用：
  - DFBS-INFRA-UP.ps1：只启动基础容器（docker compose up -d）并输出 docker ps
  - DFBS-APP-RUN.ps1：只启动后端（mvnw spring-boot:run），实时输出并写日志，不闪退
  - DFBS-HEALTHZ.ps1：只检查 /api/healthz（使用 -UseBasicParsing，避免交互安全提示）
  - DFBS-TEST.ps1：只运行 mvnw clean test（已验证：依赖基础容器必须处于可用状态）
  - DFBS-DEV.ps1：安全编排按钮（不包含 git pull，不包含 test），自动弹出 HEALTHZ 窗口并保持 APP-RUN 在当前窗口运行
  - DFBS-END.ps1 / DFBS-GEN-PROJECT-FILES.ps1 / DFBS-NEW-MODULE.ps1：均已迁移为 PowerShell，并通过验收

- DFBS-GIT-PULL.ps1：
  - 增加“必须输入 YES 才执行 git pull”的人工确认门，避免误触导致工作区状态变化

- 运行态治理：
  - 新增 DFBS-STOP-APP.ps1：停止占用 TCP 8080 的 LISTENING 进程，解决端口占用导致 APP-RUN 启动失败的问题（变量名避免与 PowerShell 内置 $PID 冲突）

### 验收标准（已通过）
- 冷启动（重启电脑后）执行 DFBS-DEV.ps1：INFRA 启动成功，APP-RUN 启动正常，HEALTHZ 检查成功
- 执行 END 后再次执行 DFBS-DEV.ps1：如遇 8080 占用，先执行 DFBS-STOP-APP.ps1 后可恢复正常
- DFBS-GIT-PULL.ps1：END 后执行 pull，无异常，且具备 YES 确认门
- 所有脚本输出日志落地到 logs/，脚本窗口不闪退，可追溯

### 约束
- 暂不删除 .bat：先保留作为应急回退入口；后续单独作为“清理批次”推进（避免一次改动过大引发返工）
### 3.27 logs 自动清理（DFBS-LOG-CLEAN.ps1）✅ 封板
- 背景：根目录 logs/ 每次脚本运行都会生成 dfbs-*.log，便于追溯但会持续增长。
- 决策：新增 DFBS-LOG-CLEAN.ps1，仅清理项目根目录 logs\dfbs-*.log，默认保留最近 200 个；不影响 backend/dfbs-app/logs/。
- 文件：DFBS-LOG-CLEAN.ps1
- 验收：双击 DFBS-LOG-CLEAN.ps1，输出删除数量；logs 下 dfbs-*.log 总数 <= Keep；后续 DFBS-DEV/TEST 仍可正常生成新日志。


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
