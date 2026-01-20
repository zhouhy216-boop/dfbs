# DFBS 项目状态（v2.1_final 冻结基准）

> 本文件只记录【已封板的最终结论】。不记录过程、不记录废弃方案。
> 工程推进原则：一次只推进一个工程级决策；推进后即封板，不回头。
> 最高优先级：避免返工；不引入新业务假设；不新增 v2.1_final 之外需求。

---

## 0. 项目基础事实（冻结）

- 项目类型：单公司主体内部管理系统（非多租户）
- 架构模式：模块化单体
- 分层结构：platform / modules(domain) / application / interfaces
- 技术栈：
    - Java 21 + Spring Boot 3.x + Maven（mvnw）
    - PostgreSQL 16 / Redis / RabbitMQ / MinIO（Docker）
    - Flyway
- 本地环境：Windows 11 + Docker Desktop + WSL2

---

## 1. 协作与输出约定（冻结）

- 面向对象：纯新手（看不懂代码）
- 助手输出必须满足：
    - 明确、可照抄的操作步骤（改哪个文件 / 粘贴什么 / 如何验收）
    - 文件路径必须可复制（与代码一致，避免手输错误）
    - 默认给出【最省事方案】（尽量“一次覆盖整文件/一次运行一个脚本”）
- 需要前置信息时：必须明确让用户贴哪个文件或哪段内容
- 本约定与 v2.1_final 同级，不得违反
### 1.1 已有文件修改前置规则（冻结）✅ 封板

- 当需要修改任何【本地已存在的代码文件】时：
  - 若该文件的完整内容未在当前对话中给出，必须先由用户上传该文件的完整原始内容。
  - 在未获得原文件全文之前，禁止直接给出修改方案或替换代码。
- 助手仅允许在以下前提下输出修改指令：
  - 基于用户上传的原文件内容；
  - 输出形式必须为：
    1) 可整体替换的完整文件，或
    2) 明确到文件路径 + 行号的最小修改说明。
- 未提供原文件全文的情况下，一律视为“不可修改状态”。

> 目的：避免因本地源文件不一致导致的反复返工与误修改。
### 1.2 需求边界与冻结基准确认规则（冻结）✅ 封板

- v2.1_final 的唯一权威需求与结构基准文件，统一存放于目录：
  - docs/baseline/

- v2.1_final 的唯一权威基准文件清单：
  - docs/baseline/final_01_mvp_scope_v2_1_final_full.md
  - docs/baseline/final_02_module_map_v2_1_final_full.mmd
  - docs/baseline/final_03_project_structure_v2_1_final_full.txt

- 禁止新增、复制或维护任何其它“基准文件”副本，避免多份文件不一致导致返工。

- 当实现或修改代码涉及以下内容，且上述基准文件中【未明确写出】时：
  - 行为逻辑
  - 字段定义
  - 业务流程
  - 页面/接口结构
  - 状态与状态流转
  - 权限规则
  - 计费/报表口径
  - 外部系统交互或扩展点

  必须遵循以下流程：
  - 助手需先用一句话明确说明“将要新增或引入的点是什么”
  - 明确向用户请求确认：“做 / 不做 / 暂不做”
  - 在用户确认之前，禁止继续落代码或给出实现方案

> 目的：确保所有实现严格受控于 v2.1_final，避免隐性新增需求与结构性返工。
- 字段命名映射（冻结）：
  - 对外（接口/报表/页面/导入导出）统一使用：customerNo
  - 对内（Entity/DB 字段）保留：customerCode
  - 映射规则固定为：customerNo ⇄ customerCode
  - 禁止在对外层出现 customerCode；禁止在对外 JSON 出现 code/customerCode 等别名

---

## 2. 跨电脑路径/盘符不一致允许 ✅ 封板

- 结论：允许不同电脑将 dfbs 项目放在不同磁盘与路径（例如 C:\dfbs / D:\dfbs），不影响开发与验收。
- 前提：
    - 代码以 Git 仓库为唯一一致性来源（main 分支同步）。
    - 所有本地脚本使用相对路径（PSScriptRoot/%~dp0）定位项目根目录，不写死绝对路径。
- 禁止事项：
    - 不允许脚本写死盘符/绝对路径（如 C:\dfbs\...）。
    - 不允许手动拷贝文件夹作为同步方式（以 Git 为准）。
- 验收方式（ps1 时代）：
    - 在任意路径的项目根目录依次执行：DFBS-INFRA-UP.ps1 → DFBS-APP-RUN.ps1 → DFBS-HEALTHZ.ps1 → DFBS-TEST.ps1 均可通过。
    - DFBS-END.ps1 可完成 git add/commit/push（无改动时允许提示 No changes to commit）。
    - DFBS-GIT-PULL.ps1 需人工输入 YES 才执行，避免误触造成工作区变化。

---

## 3. 业务模块推进顺序（地基 -> 承重 -> 上层）✅ 封板

- 冻结推进顺序（仅冻结先后，不进入字段/表/API 设计）：
    1) 主数据（customer / contract / product / machine / iccid）
    2) 报价（quote）
    3) 发货（shipment）
    4) 售后 / 维修 / 收费
    5) 平台费 / ICCID 计费
    6) 报表 / 审计 / 对外接口
- 目的：依赖自底向上落地，减少返工风险

---

## 4. 分层与模块结构（已封板）

### 4.1 分层路径冻结
- interfaces：Controller（HTTP 入口）
- application：Service / 用例（业务编排与事务边界）
- modules：Entity / Repository（领域模型与数据访问）
- platform：通用基础设施（工具、通用能力）

### 4.2 依赖方向冻结（ArchUnit 守门）
- interfaces → application → modules → platform
- 禁止反向依赖与跨层乱引用
- ArchUnit 作为 CI 级守门规则

---

## 5. 主数据模块（已封板）

### 5.1 主数据拆分
- customer / contract / product / machine / iccid

### 5.2 主数据模块内关系冻结（字段级之前最后一道关）✅ 封板
- 职责冻结：
    - customer：客户身份与唯一编号；不包含合同/设备/ICCID；不包含平台费 org_code
    - contract：合同编号与引用 customer；不包含机器/ICCID 清单
    - product：型号定义；不包含客户/合同/ICCID
    - machine：引用 contract 与 product；客户只能通过 contract 间接关联
    - iccid：引用 machine（允许为空/解绑）；不直接引用 customer/contract/product
- 引用方式冻结：只允许用对方“业务主键”引用（不引用名称等描述字段）
- 软删除冻结：deleted_at 作为软删除标记；业务主键不复用

### 5.3 引用方向冻结
- contract → customer
- machine → contract / product
- iccid → machine
- 禁止反向引用、禁止双向耦合

### 5.4 Flyway 建表 + 最小映射（V0004）
- 建表：md_customer / md_contract / md_product / md_machine / md_iccid
- 最小 Entity/Repo 映射已落地并验收通过

### 5.5 主数据 Repo 默认只读规则（守门）
- 业务代码默认不允许直接写 Repo（减少乱写导致返工）
- ArchUnit 自动化守门：限制主数据 Repo 依赖范围，并排除测试（DO_NOT_INCLUDE_TESTS）
- mvnw clean test 通过
- 写入限制（守门语义）：主数据 Repo 的写方法（save / saveAll / delete* 等）仅允许在对应模块的 <Module>MasterDataService 中调用；其他任何类禁止直接调用。
- 禁止物理删除：主数据表不允许物理 delete；删除语义统一使用 deleted_at 软删除实现（包括批量删除）。
- 验收：通过 ArchUnit/测试守门，确保除 MasterDataService 外无对主数据 Repo 的 save/delete 调用。

### 5.6 主数据“写入口”占坑（MasterDataService）✅ 封板
- 每个主数据模块建立唯一写入口：
    - application/<module>/<Module>MasterDataService
- 仅最小空实现，占位不引入业务逻辑
- 不破坏主数据 Repo 只读守门规则
### 5.7 Customer 主数据写入口（最小写入闭环）✅ 封板

- 写入口唯一性：
  - Customer 写入只能通过 CustomerMasterDataService
  - 禁止在任何其它位置调用 CustomerRepo.save/delete*（ArchUnit 守门）

- API（对外）字段命名冻结：
  - 对外（接口/报表/页面/导入导出）统一使用：customerNo
  - 对内（Entity/DB 字段）保留：customerCode
  - 映射规则固定为：customerNo ⇄ customerCode
  - 禁止对外出现 customerCode / code / customerNo 的多套别名

- 主键策略冻结：
  - CustomerEntity.id 为手动赋值型
  - CustomerMasterDataService 在保存前必须生成 UUID 并 setId

- 最小写入闭环验收标准：
  - DFBS-TEST.ps1 输出 BUILD SUCCESS
  - CustomerMasterDataCreateTest 通过
  - ArchitectureRulesTest / MasterDataReadOnlyRulesTest 通过

---

## 6. 报价模块（已封板）

### 6.1 报价版本唯一生效（数据库级）
- 同一 quote_no 任意时刻仅 1 条 is_active = true
- 落地：Flyway 迁移（部分唯一索引/约束）
- 验收：数据库层强制保证

### 6.2 报价版本 activate 语义
- activate 不新增记录，仅切换状态
- active_at 更新，created_at 不变
- 事务内保证同一 quote_no 下仅 1 条 active

### 6.3 activate 分层原则
- Controller：仅 HTTP 入口
- Service：事务与并发语义
- Repo：纯数据访问

### 6.4 自动化验收
- 测试独立生成数据，不依赖历史库
- 多次 activate 后仍仅 1 条 active
- mvnw clean test → BUILD SUCCESS

---

## 7. 本地自动化（最终形态）✅ 封板

### 7.1 统一为 PowerShell
- 工程脚本以 .ps1 为唯一执行源
- .bat 作为应急回退入口（后续单独“清理批次”再删除）

### 7.2 单职责脚本划分（禁止隐式副作用）
- DFBS-INFRA-UP.ps1：启动 Docker 基础设施（docker compose up -d + docker ps）
- DFBS-APP-RUN.ps1：启动后端（mvnw spring-boot:run），实时输出+日志，不闪退
- DFBS-HEALTHZ.ps1：健康检查（/api/healthz；UseBasicParsing，避免交互安全提示）
- DFBS-TEST.ps1：mvnw clean test（实时输出+日志，不闪退）
- DFBS-GIT-PULL.ps1：git pull（必须输入 YES 才执行，防误触）
- DFBS-END.ps1：git add / commit / push（提交收尾）
- DFBS-GEN-PROJECT-FILES.ps1：自动生成/更新 PROJECT_FILES.md（禁止手写）
- DFBS-NEW-MODULE.ps1：标准化生成新模块骨架（禁止手工创建目录）
- DFBS-STOP-APP.ps1：停止占用 TCP 8080 的 LISTENING 进程（防端口占用导致启动失败）
- DFBS-DEV.ps1：安全开发编排（不含 git pull / 不含 test；可选自动弹出 HEALTHZ 窗口）

### 7.3 自动化约束
- 不存在“一键隐式改变状态”的脚本（尤其禁止把 git pull 混入开发启动）
- 所有脚本输出日志到根目录 logs/，便于追溯
- 所有脚本默认不闪退（失败也能看到原因与日志路径）

---

## 8. 日志治理（已封板）

### 8.1 日志生成原则
- 每次运行生成独立 dfbs-*.log（避免覆盖与文件占用）
- logs/ 为工程脚本日志目录

### 8.2 自动清理策略 ✅ 封板
- DFBS-LOG-CLEAN.ps1：仅清理根目录 logs/dfbs-*.log
- 默认保留最近 200 个日志文件
- 不影响应用自身日志目录（如 backend/dfbs-app/logs/）

---

## 9. 当前冻结状态总结

- 需求基准：v2.1_final（冻结）
- 架构分层：冻结 + ArchUnit 守门
- 业务推进顺序：冻结
- 主数据模块边界/引用/软删除/引用方式：冻结
- 主数据 Repo 只读守门：冻结
- 本地自动化与脚本形态：冻结
- 日志治理：冻结

下一步允许推进：主数据写逻辑（从 customer 开始），严格通过 MasterDataService 写入口，不破坏守门规则。
