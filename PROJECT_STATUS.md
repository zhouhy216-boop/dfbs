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

## 1. 协作与输出约定（冻结）✅ 封板

### 1.1 输出对象与基本要求
- 面向对象：**纯新手（看不懂代码）**
- 助手输出必须满足：
  - 明确、可照抄的操作步骤（改哪个文件 / 粘贴什么 / 如何验收）
  - 文件路径必须可复制，且必须与代码仓库真实路径一致（避免手输或猜测）
  - 默认给出【最省事方案】（优先“一次覆盖整文件 / 一次只运行一个脚本”）
- 需要任何前置信息时：
  - 必须明确指出：**需要你上传哪个文件，或粘贴哪一段内容**
- 本约定与 **v2.1_final** 同级，任何实现与回答均不得违反

---

### 1.2 事实约束规则（冻结）✅ 封板
> 本规则用于防止“助手臆测项目结构 / 文件位置”导致的工程错误

- 涉及以下事实判断时：
  - 本地文件路径
  - 目录结构是否存在
  - 文件是否存在 / 是否已创建
- **一律以 `PROJECT_FILES.md` 或用户上传的真实文件内容为唯一事实依据**
- 助手 **禁止臆测** 项目目录或文件结构  
  （例如：猜测 `scripts/` 是否存在、猜测脚本位置等）
- 若事实不确定：
  - 助手必须先要求用户上传 `PROJECT_FILES.md`
  - 或要求用户上传/粘贴相关目录或文件
  - 在事实明确之前，**禁止继续给出修改建议或结构说明**

---

### 1.3 文件修改前置规则（Strict）（冻结）✅ 封板
> 本规则用于防止“在不了解原文件内容的情况下给出错误修改方案”

- 当需要修改任何**已有文件**（代码 / 配置 / 脚本 / 文档）时：
  - 若助手 **未获得该文件的完整原始内容**（由用户上传或粘贴）
  - 一律视为：**不可修改状态**
- 在未获得原文件全文之前：
  - ❌ 禁止直接给出“整文件替换方案”
  - ❌ 禁止假设文件内部结构或已有内容
- 助手在获得原文件全文后，只允许以下两种输出形式之一：
  1) **整文件替换版全文**（基于原文件修改后给出）
  2) **最小修改说明**（必须明确到：文件路径 + 具体位置 + 需要粘贴的内容）
- 目的：
  - 避免因本地文件与助手假设不一致，导致反复报错、误改、返工


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

---

## 10. 主数据 Customer（创建写闭环）✅ 封板

- 目标：实现最小可验收的 Customer 创建写闭环（仅 Create，不含 Update/Delete/Merge/Import）。
- HTTP：POST /api/masterdata/customers
- 分层：
  - interfaces：CustomerMasterDataController（仅 HTTP 入口）
  - application：CustomerMasterDataService（事务边界 + 唯一写入口）
  - modules：CustomerEntity / CustomerRepo（数据模型与访问）
- 约束：
  - CustomerRepo 的写入（save）仅允许发生在 CustomerMasterDataService 内
  - interfaces 不得依赖任何 Repo（ArchUnit 守门）
  - 软删除字段 deleted_at 已存在
- 验收：
  - DFBS-TEST.ps1 → BUILD SUCCESS
  - CustomerMasterDataCreateTest 通过
  - MasterDataReadOnlyRulesTest 通过

---

## 11. 主数据 Customer（Soft Delete）✅ 封板

- 能力：Customer 软删除（仅标记 deleted_at，不物理删除）
- HTTP：DELETE /api/masterdata/customers/{id} → 204
- 创建冲突：customerNo 已存在 → 409 Conflict（业务主键不复用，即使软删除也不允许复用）
- 规则：
  - 已删除记录不可再次删除（找不到或已删除 → 404）
  - 已删除记录不参与“未删除列表/查询”（只读查询默认过滤 deleted_at）
- 约束：
  - Repo 写入仅允许发生在 CustomerMasterDataService
  - 不允许物理 delete
- 验收：
  - DFBS-TEST.ps1 → BUILD SUCCESS
  - 删除后再次使用同 customerNo 创建 → 409

---

## 12. 主数据 Customer（Update 最小写闭环）✅ 封板

- 能力：Customer 更新（最小闭环：仅更新 name）
- HTTP：PATCH /api/masterdata/customers/{id}
- 规则：
  - 仅允许更新 name；不允许更新 customerNo/customerCode（业务主键不复用）
  - 找不到或已删除（deleted_at != null）→ 404
- 架构约束：
  - 写操作仅允许发生在 CustomerMasterDataService
- 验收：
  - DFBS-TEST.ps1 → ✅ TEST PASS (exit code=0)
  - 测试覆盖：创建后可更新 name，并返回更新后的值

---

## 13. 主数据 Customer（Read by ID 最小写闭环）✅ 封板

- 能力：按 ID 读取 Customer
- HTTP：GET /api/masterdata/customers/{id}
- 规则：
  - 仅返回未删除记录
  - 找不到或已软删除（deleted_at != null）→ 404
- 架构约束：
  - 业务异常由 Controller 映射为 HTTP 语义
  - Service 不感知 HTTP
- 验收：
  - DFBS-TEST.ps1 → TEST PASS (exit code=0)
  - 测试覆盖：
    - 创建后可按 ID 读取
    - 软删除后读取返回 404

---

## 14. 【主数据 Contract｜Create 最小写闭环】✅ 已封板

- 能力范围（冻结）：
  - Contract Create（仅创建）
  - HTTP：POST /api/masterdata/contracts → 201
  - 不包含：Read / Update / Delete / List

- 分层与约束：
  - interfaces：
    - ContractMasterDataController（仅 HTTP 入口）
  - application：
    - ContractMasterDataService（唯一写入口，事务边界）
  - modules：
    - ContractEntity / ContractRepo
  - 强约束：
    - ContractRepo 禁止在 Service 之外写入
    - Controller 不得直接依赖 Repo（ArchUnit 守门）

- 数据规则：
  - contract_no 全局唯一
  - customer_code 必须存在且未软删除
  - 软删除字段：deleted_at（本阶段未启用）

- 测试与验收：
  - ContractMasterDataCreateTest：PASS
  - 架构守门 / 只读规则：PASS
  - DFBS-TEST.ps1：SUCCESS（exit code = 0）

- 结论：
  - Contract Create 工程闭环完成
  - 封板，不回头

---

## 15. 【主数据 Product｜Create 最小写闭环】✅ 已封板

- 能力范围（冻结）：
  - Product Create（仅创建）
  - HTTP：POST /api/masterdata/products → 201
  - 不包含：Read / Update / Delete / List

- 分层与约束：
  - interfaces：
    - ProductMasterDataController（仅 HTTP 入口）
  - application：
    - ProductMasterDataService（唯一写入口，事务边界）
  - modules：
    - ProductEntity / ProductRepo
  - 强约束：
    - ProductRepo 禁止在 Service 之外写入
    - Controller 不得直接依赖 Repo（ArchUnit 守门）

- 数据规则：
  - product_code 全局唯一
  - 软删除字段：deleted_at（本阶段未启用）

- 测试与验收：
  - ProductMasterDataCreateTest：PASS
  - 架构守门 / 只读规则：PASS
  - DFBS-TEST.ps1：SUCCESS（exit code = 0）

- 结论：
  - Product Create 工程闭环完成
  - 封板，不回头

---

## 16. 【主数据 Machine｜Create 最小写闭环】✅ 已封板

- 能力范围（冻结）：
  - Machine Create（仅创建）
  - HTTP：POST /api/masterdata/machines → 201
  - 不包含：Read / Update / Delete / Bind / Unbind / List

- 分层与约束：
  - interfaces：
    - MachineMasterDataController（仅 HTTP 入口）
  - application：
    - MachineMasterDataService（唯一写入口，事务边界）
  - modules：
    - MachineEntity / MachineRepo
  - 强约束：
    - MachineRepo 禁止在 Service 之外写入
    - Controller 不得直接依赖 Repo（ArchUnit 守门）

- 数据规则：
  - machine_sn 全局唯一
  - contract_no 必须存在
  - product_code 必须存在
  - 仅允许通过业务主键关联（不使用 id）
  - 软删除字段：deleted_at（本阶段未启用）

- 测试与验收：
  - MachineMasterDataCreateTest：PASS
  - 架构守门 / 只读规则：PASS
  - DFBS-TEST.ps1：SUCCESS（exit code = 0）

- 结论：
  - Machine Create 工程闭环完成
  - 封板，不回头


---

## 17. 【主数据 ICCID｜Create 最小写闭环】✅ 已封板

- 能力范围（冻结）：
  - ICCID Create（仅创建）
  - HTTP：POST /api/masterdata/iccid → 201
  - 支持：machine_sn 为空（允许未绑定/解绑）
  - 不包含：Read / Update / Delete / Bind / Unbind / List

- 分层与约束：
  - interfaces：
    - IccidMasterDataController（仅 HTTP 入口）
  - application：
    - IccidMasterDataService（唯一写入口，事务边界）
  - modules：
    - IccidEntity / IccidRepo
  - 强约束：
    - IccidRepo 禁止在 Service 之外写入
    - Controller 不得直接依赖 Repo（ArchUnit 守门）

- 数据规则：
  - iccid_no 全局唯一
  - machine_sn 允许为空；若不为空，则必须存在
  - 仅允许通过业务主键关联（不使用 id）
  - 软删除字段：deleted_at（本阶段未启用）

- 测试与验收：
  - IccidMasterDataCreateTest：PASS
  - 架构守门 / 只读规则：PASS
  - DFBS-TEST.ps1：SUCCESS（exit code = 0）

- 结论：
  - ICCID Create 工程闭环完成
  - 封板，不回头



### 报价/回款/仓库/财务/货损高风险规则（冻结）✅ 封板

- 报价自动建议停止死线：
  - 报价单一旦首次递交，系统必须永久停止价格自动建议；此后只能人工修改（并记录审计）
- 回款核销跨周期分配：
  - 允许跨周期：Payment 与 Allocation 可属于不同会计周期（Period）
  - Allocation 数据结构必须包含：allocated_amount、period_id
- 仓库两级结构 + 出库绑定报价明细：
  - 两级仓库：服务部大库 ↔ 服务经理小库（补货/领用/归还需留痕）
  - 出库必须绑定报价明细（明细级出库）
- 收入 vs 成本双口径分离：
  - 同一物流事件需同时记录“报价收入”和“报销成本(Expense)”两套口径；两者必须分离
- 货损独立性：
  - 货损(Damage)台账与维修/收费完全独立，不自动联动（避免业务耦合）
