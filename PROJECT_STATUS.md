# PROJECT_STATUS.md（DFBS 唯一推进状态文件｜v2.1_final｜四人共读版）

用途：
- 本文件是 DFBS 项目的【唯一推进状态源】与【封板记录】。
- 仅记录：已冻结规则、、里程碑流水账（只追加）。
- 不记录：对话话术、个人待办清单、排期计划、代码全文细节（代码细节在工单/提交里）。
## 全局规则（必须遵守）：
- 里程碑只允许【追加】，不允许回头改历史（发现错误用“勘误”追加说明）。
- 同一天多条里程碑，用序号区分：`YYYY-MM-DD-01 / 02 / 03 ...`
- 每次 User 验收通过（Tests Passed），必须追加一条里程碑并标记“✅ 封板”。
- 从现在开始：更新只做一件事
  1) 在【D. 里程碑流水账】末尾追加一条
- 当测试全部通过 (BUILD SUCCESS) 且任务目标达成时。 Cursor必须**主动、自动**地修改 `PROJECT_STATUS.md`PROJECT_STATUS.md 的里程碑。


---

## A. 唯一权威基准（冻结）

- 需求边界（不可更改）：`docs/baseline/` 下 `final_01 / final_02 / final_03`（v2.1_final）
- 工程真实目录（事实唯一来源）：`PROJECT_FILES.md`
- 推进与封板（唯一约束）：本文件 `PROJECT_STATUS.md`

---

## B. 四人协作分工（冻结）

- **User（CEO / 验收者）**
  - 负责：需求确认、测试验收（通过/不通过）、提供报错/现象
  - 不负责：写代码、决定技术实现

- **PM（ChatGPT）**
  - 负责：需求采访（小步快走）、逻辑冻结（纯文字）、维护本文件（推进点 + 里程碑封板）
  - 不负责：输出“改哪个文件/写什么代码/怎么跑测试”（由 Gemini/Cursor 负责）

- **Gemini（Tech Lead / 翻译官）**
  - 负责：把 PM 的“冻结逻辑文字”转成 Cursor 可执行的工程指令（包含文件路径、修改点、测试方案）
  - 不负责：最终写代码（除非临时要求）

- **Cursor（Engineer / 执行）**
  - 负责：按 Gemini 指令修改代码、跑测试、修到全绿
  - 输出必须可复制（路径/命令/必要时代码全文）

---

## C. 客观进度快照（只写事实，不写计划）

### C1. 主数据（MasterData）
- 已存在主数据实体/表：Customer / Contract / Product / Machine / ICCID
- 已封板能力（以里程碑为准）：
  - Create 最小闭环：Customer / Contract / Product / Machine / ICCID ✅
  - 列表 + 关键字搜索：Customer / Contract / Product ✅

### C2. 报价（Quote）
- 已封板：
  - QuoteVersion：版本唯一生效（激活规则）✅
  - Quote：核心实体 + 编号计数 + 编号服务 + 状态机（DRAFT/CONFIRMED/CANCELLED）+ API + 测试 ✅
  - QuoteItem：明细行 CRUD + 金额自动计算 + 单位建议 + 仓库总部提醒 + DRAFT 状态保护 + 数据迁移兼容 + 测试 ✅
- 未封板（仍未完成）：
  - 报价单导出 Excel 模板闭环
  - 从维修工单 / 平台费 / 委托运输发起报价单的“自动填充（抬头+明细）”
  - 费用类型/描述/单位的“管理员可配置字典”
  - 预留自定义字段的“启用/配置/展示”

---

## D. 里程碑流水账（Append Only）

> 规则：只追加。每条必须写清：做了什么、验收方式、结论（是否封板）。

### 2026-01-19-01
- 主数据模块内关系冻结（Customer / Contract / Product / Machine / ICCID）
- 删除口径冻结：禁止物理删除（软删除）
- 主数据写入口限制规则确认
- 结论：✅ 封板

### 2026-01-22-01
- 主数据 Create API 全部通过测试
- 守门/只读规则相关测试全绿
- 结论：✅ 封板

### 2026-01-23-01
- QuoteVersion：版本唯一生效规则完成（数据库约束 + Service.activate + Test）
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-23-04（主数据 A 类关键缺口补齐：列表与搜索）
- 范围：Customer / Contract / Product
- 完成内容：
  1) Repo 增强：支持条件查询（用于搜索）
  2) API：实现列表 + keyword 模糊搜索，并自动过滤软删除
  3) 测试：修复“脏数据干扰”导致的不稳定问题
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-23-05（报价单 Quote 最小闭环：实体与状态机 Entity & Workflow）
- 完成内容：
  1) 核心实体：QuoteEntity（抬头）、QuoteSequenceEntity（编号计数）
  2) 编号服务：Prefix + User + Date + Seq；支持按“月 + 发起人”重置/递增
  3) 状态机：DRAFT（可改）-> CONFIRMED（锁定）/ CANCELLED（作废）
  4) API 与测试：接口就绪；QuoteNumberingTest + QuoteStateTest 全部通过
- 验收：Tests Passed（User 验收通过）
- 结论：✅ 封板（不回头）

### 2026-01-23-06（报价单 QuoteItem 明细最小闭环 MVP）
- 完成内容（由 Gemini + Cursor 落地）：
  1) 核心实体：QuoteItemEntity 已创建并关联 QuoteEntity
  2) 业务逻辑：明细行 CRUD（增删改查）
  3) 自动计算：单价 × 数量 = 金额（保留 2 位小数）
  4) 智能建议：选择费用类型后自动带出默认单位（可作为建议值）
  5) 仓库提醒：选择“总部”仓库时，接口返回提醒消息
  6) 状态保护：仅允许在 Quote = DRAFT 状态下修改明细
  7) 数据迁移：V0007 处理新旧表结构兼容，并清理旧字段约束（quote_version_id、item_type 等）
  8) 测试验证：QuoteItemTest 全绿，覆盖上述规则
- 验收：Tests Passed（User 验收通过）
- 结论：✅ 封板（不回头）

### 2026-01-26-01（报价单导出（Excel/PDF）最小闭环）
- 完成内容（由 Gemini + Cursor 落地）：
  1) 工程与依赖：引入 Apache POI（Excel）与 Spire.XLS Free（PDF）；修复 Java 21 下 Spire 缺失 JAXB 依赖导致的 NoClassDefFoundError
  2) 公司信息配置：CompanyInfoProperties 读取公司银行/税务信息（导出统一引用）
  3) 导出服务：QuoteExportService 基于“报价单模板v3.xlsx”精确填充
     - 动态行：不足 9 行清空占位符；超过 9 行插入新行并复制样式，保持版式不乱
     - 币种：CNY 输出中文大写金额与 ￥；USD/JPY 切换“美元/日元”文本与 $/JP¥ 符号
  4) PDF：Excel 转 PDF 的流式输出
  5) API：GET /api/v1/quotes/{id}/export?format=xlsx|pdf
     - 状态校验：仅 DRAFT / CONFIRMED 允许导出
  6) 测试：QuoteExportTest 全绿（小数据/大数据、PDF 流、状态拦截等场景）
- 验收：Tests Passed（User 验收通过）
- 结论：✅ 封板（不回头）

### 2026-01-26-02（工单发起报价单 Work Order to Quote MVP）
- 完成内容（由 Gemini + Cursor 落地）：
  1) 领域模型更新：QuoteEntity 新增 `sourceId`（工单号）、`machineInfo`（机器快照）、`assigneeId`（服务经理ID）；枚举 QuoteSourceType 新增 `WORK_ORDER`
  2) 数据迁移：V0008 已执行
  3) 自动生成逻辑（QuoteService）：新增 `createFromWorkOrder`
     - 抬头自动填充：客户/收件人/机器信息带入；创建人=派单员；接手人=服务经理
     - 明细自动生成：固定 1 行“维修费”；isOnsite=true 追加“上门费”；parts 追加对应“配件费”行
  4) 导出增强：QuoteExportService 落款优先使用 assigneeId（服务经理）资料，而非创建人
  5) 工程优化：模板改名 `quote_template_v3.xlsx`；新增 `UserInfoProvider` 用于获取用户详情
  6) 测试验证：WorkOrderQuoteTest 全绿（无配件/有配件/上门/非上门 + Header 映射）
- 验收：Tests Passed（User 验收通过）
- 结论：✅ 封板（不回头）

### 2026-01-26-03（报价单费用字典 + 配件BOM 最小闭环 MVP）
- 完成内容（由 Gemini + Cursor 落地）：
  1) 领域模型：新增 FeeCategory / FeeType / Part / ProductBom 实体
  2) 数据迁移：执行 V0009，并预置默认字典数据（含“技术服务费、登门费、月/年”等）
  3) 字典服务：实现字典 CRUD + 停用替代逻辑（停用 A 必须指定启用的 B）；实现 BOM 维护服务
  4) 核心集成：DRAFT 允许费用类型/规格手填；CONFIRM 前强校验字典有效性；确认时单位不合法自动回默认；createFromWorkOrder 映射旧“维修费/上门费/配件费”到新字典 ID
  5) API 与测试：新增 DictionaryController / PartController；回归测试全绿；DictionaryQuoteTest 覆盖“草稿自由填、确认强阻断、确认成功、停用逻辑、单位自动修”等 6 场景
- 验收：Tests Passed（User 验收通过）
- 结论：✅ 封板（不回头）

### 2026-01-26-04（报价单回款记录 PaymentRecord MVP）
- 完成内容（由 Gemini + Cursor 落地）：
  1) Domain/DB：新增 QuotePayment / PaymentMethod / QuoteCollectorHistory；QuoteEntity 扩展 paymentStatus、collectorId、parentQuoteId；迁移 V0010 预置“对公/微信/支付宝”
  2) Service：仅 CONFIRMED 可提交回款；金额>0；回款时间不晚于当前；财务确认/退回；超额二选一并可自动生成关联 DRAFT 差额报价单
  3) 状态与锁死：paymentStatus 自动 UNPAID→PARTIAL→PAID；PAID 后锁死收款执行人并禁止变更
  4) API：新增 PaymentController；工单转报价支持指定 collectorId
  5) Tests：PaymentRecordTest 覆盖 6 场景；WorkOrderQuoteTest 回归全绿
- 验收：Tests Passed（User 验收通过）
- 结论：✅ 封板（不回头）

### 2026-01-26-05（报价单作废申请与审批 Quote Void Process MVP）
- 完成内容（由 Gemini + Cursor 落地）：
  1) Domain/DB：新增 QuoteVoidApplication；QuoteEntity 新增 voidStatus（NONE/APPLYING/VOIDED/REJECTED）；迁移 V0011
  2) Service：
     - 申请流：仅收款人可发起；同一时间仅一条未结案申请
     - 冻结拦截：voidStatus=APPLYING 时拦截“编辑报价单”与“提交/确认回款”（抛“审批中已冻结”）
     - 审批流：同意→报价单进入 CANCELLED 最终态永久冻结；驳回→释放冻结允许再申请；财务直作废→直接 CANCELLED
     - 工单联动：工单“已报价”动态判定（存在非 CANCELLED 报价单即已报价；全部作废回滚未报价）
  3) API：新增 QuoteVoidController（申请/审批/直作废/历史查询）
  4) Tests：QuoteVoidTest 覆盖权限、申请冻结、审批流转、直作废、工单回滚等 6 场景
- 验收：Tests Passed（User 验收通过）
- 结论：✅ 封板（不回头）

### 2026-01-27-01（报价单抄送领导 Quote CC Leadership MVP）
- 完成内容（由 Gemini + Cursor 落地）：
  1) Domain/DB：新增 BusinessLine（业务线→领导名单）与 Notification（站内信）；QuoteEntity 新增 businessLineId；执行 V0012 并预置默认业务线
  2) 触发规则：在报价单 CONFIRM 成功后，按业务线配置向领导发送站内信；未配置则静默跳过不阻断流程
  3) 通知内容：包含报价单号/客户/金额等摘要，支持跳转到报价单详情页
  4) API：新增 BusinessLineController（超管配置）与 NotificationController（用户查看）；QuoteController 支持创建/编辑时选择业务线
  5) Tests：QuoteCcTest 覆盖“正常抄送/无配置静默/内容校验/列表获取”等；WorkOrderQuoteTest 回归全绿
- 验收：Tests Passed（User 验收通过）
- 结论：✅ 封板（不回头）

### 2026-01-27-02（发件报价单抄送仓库 Quote CC Warehouse MVP）
- 完成内容：
  1) Domain/DB：新增 WarehouseConfig（单例，存仓库人员名单）；QuoteEntity 新增 isWarehouseCcSent / isWarehouseShipSent 去重标记；执行迁移 V0013
  2) 核心逻辑：
     - 抄送触发：报价单创建/编辑后，若包含 HEADQUARTERS（总部）仓库明细且未发送过 → 自动通知仓库
     - 发货提醒：报价单确认后，若包含总部明细且未发送过 → 自动通知仓库“请安排发货”
     - 去重机制：严格基于数据库字段去重，防止重复通知
  3) API：新增 WarehouseConfigController 用于配置仓库人员
  4) 架构治理：引入 PaymentMethodService 修复 Controller 直连 Repository；WorkOrderImportRequest DTO 从 Interface 层移至 Application 层；11 个集成测试迁移至 application 包并修复中文乱码
  5) Tests：QuoteWarehouseTest 覆盖配置/创建触发/非触发/去重/确认发货提醒/延迟触发等 6 场景；全量回归测试 75 个 Test 全绿（BUILD SUCCESS）
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-28-01（报价单回款确认流程 MVP）
- 完成内容：
  - 工作流状态机：新增 APPROVAL_PENDING（待审核）与 RETURNED（已退回）状态；实现发起人/跟单人提交 → 财务审核（通过/驳回）→ 财务指派收款人 → 收款人录入 → 财务确认到账的完整流转
  - 资金核算与安全：
    - 双重确认：收款人录入默认为 Unconfirmed，仅财务“确认到账”后才扣减未收金额
    - 自动核销：基于已确认金额自动判定 PARTIAL（部分回款）/ PAID（已回款）
    - 架构预留：QuotePaymentEntity 预埋 paymentBatchNo，为未来“合并回款”预留
  - 审计追溯：新增 QuoteWorkflowHistoryEntity，记录提交/退回/作废/确认的操作人、时间、原因与状态变迁
  - Tests：全量回归 80/80 全绿；QuotePaymentWorkflowTest 覆盖正常流程、财务驳回、超额支付校验、已付款后作废等复杂场景
- 验收：Tests Passed（80/80）
- 结论：✅ 封板

### 2026-01-28-02（收款执行人：待回款清单 + 合并回款 MVP）
- 完成内容：
  - 待回款清单：
    - 新增 listMyPendingQuotes（JPA Specification 精准查询）
    - 过滤：仅显示“财务已指派给我”且“未付清”的确认态报价单；自动过滤已撤销/草稿/非本人指派单据
    - 输出字段：报价单号、客户名、未收金额、币种等关键字段供前端展示
  - 合并回款：
    - 批次关联：使用 paymentBatchNo 将一次打款生成的 N 条回款记录关联
    - 严格校验：客户/币种/执行人必须一致；打款金额必须严格等于所选未收金额合计（多一分少一分均拦截并提示差额）
    - 状态安全：合并生成记录默认未确认，不绕过财务确认
  - 稳定性修复：修复 QuoteService 编译问题；异常提示文案与需求文档对齐
  - Tests：QuoteBatchPaymentTest 全绿，覆盖正常合并/跨客户失败/金额不匹配拦截/已付清拦截等核心场景
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-28-03（报价单发票申请分支流程 含合并/拆分 MVP）
- 完成内容：
  - 领域建模：建立 Application -> Record -> ItemRef 三层实体结构，支持“一次申请含多张发票、一张发票含多个费用项”；QuoteEntity 新增 invoicedAmount（已开票金额）与 invoiceStatus（开票状态）
  - 核心逻辑：
    - 合并开票：支持多选同一客户/同一执行人/同一币种报价单合并申请
    - 按需拆分：一次申请内按不同税率/类型将费用项拆分到不同 Invoice Record
    - 金额风控：严格校验 本次申请金额 + 已开票金额 <= 报价单总额，防止超额开票
    - 币种一致：强制校验合并开票币种一致，并修复 NPE
  - 审批与回写：实现提交→财务审批→结果通知闭环；同意后累加已开票金额并按比例更新状态（部分/已开票）；驳回后回滚状态允许重提
  - Tests：全量回归测试全绿；InvoiceApplicationTest 覆盖单张全额/多张合并拆分/跨客户拦截/金额超限拦截等场景
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-28-04（报价单作废流程 含强风控/全节点回退 MVP）
- 完成内容：
  - 强风控体系：
    - 强风控场景：报价单只要涉及“已回款（含部分）”或“已申请开票”即触发最高级别风控
    - 三级审批：强风控作废必须“收款执行人申请 → 财务审批 → 领导审批”三级同意，缺一不可
  - 灵活作废机制（分阶段鉴权）：
    - 财务确认前：发起人可直接作废；跟单人需申请作废
    - 财务确认后（普通）：财务可直接作废；收款执行人需申请作废
    - 原因强制：所有申请与回退操作必须填写原因，确保可追溯
  - 级联失效与数据清洗：
    - 作废生效时自动作废“未确认回款记录”与“进行中开票申请”，防止死单复活/虚假开票
  - 全节点回退：
    - 支持从当前状态回退到上一逻辑节点（例：待审核退回草稿），并记录操作日志
  - Tests：全量回归全绿；QuoteVoidTest 覆盖发起人自废/财务审批废/强风控三级审批（已付清&开票中）/正常节点回退等 5 场景
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-28-05（报价单反向入口：发起委托/工单 MVP）
- 完成内容：
  - 反向入口架构：不改报价单核心状态机，通过 downstreamType/downstreamId 实现从“已确认/已审核通过”报价单到下游单据的单向导流
  - 二选一互斥：同一张报价单只能发起一次下游业务（委托运输 OR 维修工单占位），重复发起强拦截
  - 下游单据落地：
    - 委托运输（Shipment）：支持发货/收货信息与装卸/包装等特殊服务字段，完成创建
    - 工单占位（WorkOrder）：完成占位记录创建，为后续对接完整维修系统预留接口
  - 风控与级联：
    - 权限：仅报价单当前负责人（Assignee）可发起下游业务
    - 级联作废：主单触发强风控作废时，自动取消关联下游单据，保证状态一致
  - Tests：全量回归全绿；QuoteDownstreamTest 覆盖发起发货/发起工单/互斥拦截/非负责人拦截/主单作废级联取消等场景
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-29-01（委托运输/发货单处理闭环：接单/发货/签收 MVP）
- 完成内容：
  - 发货单全流程状态：待接单 → 待发货 → 已发货 → 已完成
  - 支持“异常/取消”分支，且必须填写原因
  - 接单：锁定处理人 + 接单时间
  - 发货：必须补齐承运商、发货日期、联系人/电话、提货/送货地址、装卸/包装等关键信息（缺失就不让发货）
  - 签收：完成交付闭环
  - 查询：支持按状态/发起人/报价单等条件筛选清单，并可查看详情
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-29-03（发货模块全景 MVP：正常/委托双轨 + 机器号 + 智能解析）
- 完成内容：
  - 双轨发货架构：ShipmentEntity 升级支持 ShipmentType（NORMAL/ENTRUST）；createNormal/createEntrust 两套独立校验策略（合同/销售人 vs 提货信息/报价单关联）
  - 机器号管理：新增 ShipmentMachineEntity，按机型录入序列号；连号自动生成（SN001 + 数量5 → SN001~SN005）
  - 提效工具：parseText 智能解析（Regex 识别地址/电话/合同号/委托事项并按类型填充）；提货票/签收回执单支持 HTML 预览打印
  - Tests：全量回归通过；ShipmentPanoramaTest 覆盖正常/委托全流程、机器号生成、强制签字校验、报价单1:1关联、解析准确性；ShipmentProcessTest 回归通过
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-29-04（货损台账 Damage Record MVP）
- 完成内容：
  - 领域模型与配置化：
    - 引入 TreatmentBehavior（赔偿/维修/通用），使管理员配置的“处理方式”可驱动不同业务逻辑
    - 建立 DamageRecord → Shipment → ShipmentMachine 强关联链，确保货损必须锚定到具体机器号
  - 风控体系：
    - 证据强制：创建即校验附件，无照片/截图无法提交
    - 机器号校验：只能从当前发货单已录入机器中选择，杜绝脏数据
  - 业务闭环：
    - 赔偿：确认赔偿→记录金额→上传支付凭证
    - 维修：返厂→维修中→结算（含维修费/违约金明细）
  - Tests：全量回归全绿；DamageRecordTest 覆盖附件缺失/机器不匹配拦截、赔偿流程、维修流程与费用结算
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-29-05（运费账单结算 Freight Bill MVP）
- 完成内容：
  - 财务模型：新增 FreightBill（账单头）/ FreightBillItem（账单行）；账单行按“发货单×机型”自动拆分，支持不同机型分别录入单价
  - 风控：在 ShipmentEntity 增加 freightBillId（索引字段）作为防重复计费锁；发货单入账即锁定，无法被其他账单选中
  - 状态流转：草稿（可改/可剔除）→ 已确认（锁价/必传签字附件）→ 已结算
  - 交互闭环：按“承运商 + 状态 + 未锁”筛选未结算发货单并多选纳入；编辑页支持“剔除发货单→删除对应行→解锁发货单→重算总价”的级联回滚
  - 导出：支持生成 HTML 对账单
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-29-06（两级库存管理 HQ/Branch Inventory MVP）
- 完成内容：
  1) 双层库存架构：建立 HQ（总部大库）+ BRANCH（办事处小库）多仓库模型
  2) 原子库存操作：实现带并发锁的 addStock / deductStock，底层杜绝负库存
  3) 出库强绑定：新增 OutboundService，出库必须绑定“工单配件明细”或“报价单配件明细”，保证实物流与业务流一致
  4) 特殊出库：对报废/赠送等场景实现“申请 → 审批 → 执行”的独立流程
  5) 调拨闭环：实现“申请 → 发出（在途）→ 签收（入库）”完整链路
  6) 跨库策略：办事处本地有货但申请大库发货时，系统拦截并强制填写原因；并支持通知仓库管理员执行发货
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-29-07（售后维修台账 Repair Ledger MVP）
- 完成内容：
  - 历史数据导入：集成 EasyExcel 3.x 支持大批量导入；行级错误反馈（如第 N 行缺字段）
  - 去重风控：基于 OldWorkOrderNo（旧工单号）唯一性校验，重复导入拦截
  - 台账结构：固化 8 大核心字段（客户、机器号、故障/处理、保内外等）
  - 扩展预留：预留 SourceType（来源）与 WorkOrderId 字段，为未来工单自动写入预留
  - Tests：全量回归全绿；RepairRecordTest 覆盖导入/缺失拦截/去重/查询
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-29-08（权限申请 Permission Request MVP）
- 完成内容：
  - 用户与权限基础：新增 UserEntity（app_user）作为权限载体，预埋 authorities；增加 canRequestPermission 标记，仅授权用户可发起申请
  - 审计留痕：snapshotBefore / snapshotAfter 以 JSON 快照记录权限变更前后差异；审批通过时同事务完成“改权限+更新状态+保存快照”
  - 流程风控：同一人同一时间仅允许 1 个未处理申请；支持 申请→退回补充→重交→审批 的闭环
  - Tests：全量回归全绿；PermissionRequestTest 覆盖资格拦截、反刷单、状态流转、权限变更与快照比对
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-29-09（通知中心 Notification Center MVP）
- 完成内容：
  - 消息基础设施：NotificationEntity 引入 Priority（普通/紧急）、Type（业务类型）、ActionRequired（待办标记）等字段；UserEntity 增加 allowNormalNotification 开关，支持“仅接收紧急通知”
  - 核心能力：send()/sendBatch() 智能分发（按用户偏好推送）；定时清理（普通保留 180 天、紧急保留 365 天）；通过 isActionRequired 支持“需我处理”筛选；点开即已读
  - 兼容性：保留旧的 send(userId,title,content,url) 签名并映射为 NORMAL，确保既有模块零成本迁移
  - Tests：全量回归全绿；NotificationTest 覆盖免打扰拦截、紧急强制触达、待办筛选/已读标记、过期清理任务
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-29-10（关键附件必传规则 Mandatory Attachment Rules MVP）
- 完成内容：
  - 统一附件上传入口：支持照片/PDF上传，并可被各业务流程复用
  - 全局限制：单文件 ≤ 10MB；单次最多 10 个文件（避免存储被滥用）
  - 规则中心：集中配置“哪个动作必须上传哪些附件”，在提交动作时强拦截（不传不让过）
  - 已覆盖的必传场景：
    - 运费账单：确认时必须上传“签字/账单照片”
    - 总部发货/大库发货：执行时必须上传“快递/物流单”
    - 正常发货：发货时必须上传“提货票”；签收完成时必须上传“回执单”
    - 货损：创建时必须上传“货损照片”
  - 清理统一：移除各模块里零散的附件校验，统一走规则中心校验
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-30-01（客户对账单 Customer Statement MVP）
- 完成内容：
  - 对账单模型：AccountStatement（头）+ AccountStatementItem（行）；生成时用“快照模式”固化各报价单未收金额，保证对账数据一致
  - 状态：PENDING（待对账）→ RECONCILED（已对账）；只有绑定回款后才算完成
  - 生成与编辑：按“客户 + 币种”筛选未结报价单生成；PENDING 状态下允许剔除明细并自动重算总额；不允许手改金额
  - 回款强绑定：BatchPaymentRequest 增加 statementId；严格校验“回款总额 = 对账单总额”（差一分钱都拦截）；绑定回款后自动核销关联报价单
  - 架构治理：修复 Controller 直连 Repo，权限校验下沉到 AccountStatementService，测试归位
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-30-02（零部件主数据 + 装配清单 BOM MVP）
- 完成内容：
  - 零部件主数据：PartEntity 增加 systemNo（系统自动编号）、spec（规格描述）、salesPrice（标准售价）等；自动编号规则 PT-yyyyMMdd-NNN
  - Excel 导入：支持批量导入零部件；按“名称+规格或图号”自动去重并更新价格
  - BOM 版本化：BomVersion（头）+ BomItem（行）；每次导入即生成新版本，旧版本归档并保留历史；仅一个版本激活
  - 数据校验：导入时校验零部件必须存在；IndexNo 重复拦截
  - 价格偏离：封装 isPriceDeviated，多币种折算对比；只要不一致即标记
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-30-03（报价单配件明细引用零部件主数据 Part MVP）
- 完成内容：
  - 领域连接：QuoteEntity 补齐 machineId，实现“按报价单机型过滤零部件”的精准搜索；QuoteItemEntity 增加 partId + standardPrice，选择零部件时固化当时标准价，保证历史报价不受后续调价影响
  - 价格风控：选择零部件自动带出名称/规格/标准价；只要 unitPrice ≠ standardPrice 即实时标记 isPriceDeviated=true（含后续编辑改价场景）
  - 交互修复：修复“只改价格时丢失已关联零部件”的部分更新语义问题
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-30-04（Machine / ICCID 列表 + 关键字搜索 MVP）
- 完成内容：
  - Machine 列表字段：客户名称、发货日期、同批台数、机型、城市、地址、销售人、机器号、回单号
  - ICCID 列表字段：ICCID、绑定机器号、客户、合同号、机构代码、套餐、平台、到期时间
  - 支持关键字搜索：机器号、ICCID
  - 回单号规则：同一张发货单 = 同一个回单号
- 验收：Tests Passed
- 结论：✅ 封板
### 2026-01-30-05（Machine / ICCID 列表 + 关键字搜索 MVP）
- 完成内容：
  - ShipmentEntity 补齐 logisticsNo（物流/回单号）；IccidEntity 补齐 套餐/平台/到期日；Flyway：V0029
  - Machine 列表：聚合展示“最新发货日期、客户名称、回单号、同批台数（按发货单ID统计）”
  - ICCID 列表：穿透展示“绑定机器号、最终客户名称”，支持“已绑定/未绑定”筛选
  - 关键字检索：机器号（Machine SN）/ ICCID 模糊搜索
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-30-06（报价单字段字典归一 Customer + Part MVP）
- 完成内容：
  - 约束放宽：移除 quote.customer_id 非空约束，允许草稿阶段暂不关联标准客户
  - 字段补齐：新增 customer_name（临时名/快照）、original_customer_name、original_part_name（留痕可追溯）
  - 先宽后严：创建阶段允许纯文本 customerName / 配件文本描述；财务确认 QuoteWorkflowService.confirm() 强校验 customerId/partId 必须存在，否则拦截提示
  - 归一留痕：updateHeader/updateItem 从临时转标准时自动备份原始名称到 original_* 字段
  - Tests：QuoteStandardizationTest 覆盖临时创建、确认拦截、补全归一、快照留痕、放行
- 验收：Tests Passed
- 结论：✅ 封板

### 2026-01-30-07（客户资料补齐 + 合并客户 Alias MVP）
- 完成内容：
  - 客户主键 ID 类型统一：md_customer 从 UUID 迁移为 BIGINT(Long)，与核心模块一致
  - 新增 md_customer_alias：支持多别名，且全局唯一
  - 新增 md_customer_merge_log：保存合并前全量 JSON 快照，用于撤销合并
  - 局部唯一索引 uk_md_customer_name_active：仅 ACTIVE 状态下客户名唯一（MERGED/DELETED 可释放名称）
  - 客户创建/修改：严格校验客户公司全称必填 +（ACTIVE 范围）唯一
  - 合并客户：
    - 被合并客户置为 MERGED，记录 merged_to_id
    - 旧名/旧别名迁移到保留客户名下
    - 自动生成审计快照（双方全量 JSON）
  - 搜索：支持 Name LIKE %kw% OR Alias LIKE %kw%，实现“搜旧名出新主”
  - 撤销合并：基于快照反向还原；支持名称冲突预检并阻断提示
  - 工程化修复：解决 Flyway 11 测试环境 Schema 清洗与校验问题
- 验收：Tests Passed（CustomerMergeTest 覆盖：唯一性拦截、合并流转/搜索命中、撤销还原、撤销名称冲突检测；全量回归全绿）
- 结论：✅ 封板
`

### 2026-01-30-08（费用记录 + 报销申请 Expense/Claim MVP）
- 完成内容：
  - 基础设施：
    - 新增 expense 表：支持多类型（运费/差旅等）、多关联（报价单/工单/发货单等 ID 锚点）、多币种（复用现有枚举）
    - 新增 claim 表：支持一单多费、审批流转字段（submitter/approver）
    - 实现 CurrentUserIdResolver：在仅有 Username 的现状下动态解析 User ID，确保 created_by 审计字段准确
  - 核心业务逻辑：
    - 费用生命周期：仅 DRAFT 可创建/编辑；作废（VOID）；被报销单提交后置为 CLAIMED 并逻辑锁定（禁止修改/删除）
    - 报销流程：同币种多费用生成报销单；财务退回 -> RETURNED -> 费用自动解锁回 DRAFT -> 修改后可重提 -> 再次锁定
    - 权限控制：普通用户仅见自己数据；财务/管理员可见全量
- 验收：Tests Passed（ExpenseClaimTest 覆盖：费用状态流转、币种一致性拦截、退回重提全流程、数据权限隔离；全量回归全绿）
- 结论：✅ 封板

### 2026-01-30-09（出差申请 Trip Request MVP）
- 完成内容：
  - 核心业务逻辑：
    - 完整状态机：DRAFT / SUBMITTED / LEADER_APPROVED / FINANCE_APPROVED / CANCEL_REQUESTED / CANCELLED 等共 8 种状态，支持完整流转
    - “后悔药”机制：
      - 撤回（Withdraw）：提交后、领导审批前，申请人可一键撤回至草稿
      - 取消（Cancel）：财务终审通过后可发起取消申请，经领导审批同意后作废单据（逻辑删除）
    - 强校验：在 ExpenseService 植入守门员逻辑，TRANSPORT（交通）/ACCOMMODATION（住宿）费用必须关联有效出差申请，否则拒绝保存
  - 基础设施：
    - 新增 trip_request 表：支持双向入口（关联工单 / 独立填写理由），预留预算字段 est_transport_cost / est_accommodation_cost
    - 提供完整 RESTful API（含动作接口：/submit、/withdraw、/cancel-request 等）
- 验收：Tests Passed（TripRequestTest 覆盖：独立发起必填校验、正向审批流、撤回/退回逆向流、终态取消流、费用关联强约束拦截；全量回归全绿）
- 结论：✅ 封板

### 2026-01-30-08（客户资料补齐 + 合并客户 Alias MVP）
- 完成内容：
  - md_customer 主键由 UUID 迁移为 BIGINT(Long)，与核心模块一致
  - 新增 md_customer_alias：支持多别名，且全局唯一索引
  - 新增 md_customer_merge_log：保存合并前全量 JSON 快照，用于撤销合并
  - 局部唯一索引 uk_md_customer_name_active：仅 ACTIVE 状态下客户名唯一（MERGED/DELETED 可释放名称）
  - 客户创建/修改：客户公司全称强校验必填 +（ACTIVE 范围）唯一
  - 合并：被合并客户置 MERGED 并记录 merged_to_id；旧名/旧别名迁移到保留客户；生成审计快照
  - 搜索：Name LIKE %kw% OR Alias LIKE %kw%，实现“搜旧名出新主”
  - 撤销：基于快照反向还原；撤销时名称冲突预检并阻断提示
- 验收：Tests Passed（CustomerMergeTest 全绿；全量回归通过）
- 结论：✅ 封板

### 2026-01-30-09（费用记录 + 报销申请 Expense/Claim MVP）
- 完成内容：
  - 新增 expense / claim 表：支持多类型、多关联（Quote/WorkOrder/Outbound/Trip）、多币种；一单多费与审批字段
  - CurrentUserIdResolver：在仅有 Username 的现状下动态解析 User ID，确保 created_by 审计准确
  - 费用生命周期：仅 DRAFT 可改；VOID 作废；进入已提交报销单后置 CLAIMED 并锁定不可改
  - 报销流程：同币种多费用生成报销单；财务退回 RETURNED 自动解锁费用回 DRAFT；可修改后重提再锁定
  - 权限：普通用户仅见自己；财务/管理员可见全量
- 验收：Tests Passed（ExpenseClaimTest 全绿；全量回归通过）
- 结论：✅ 封板

### 2026-01-30-10（出差申请 Trip Request MVP）
- 完成内容：
  - 出差申请状态机：含 DRAFT/SUBMITTED/LEADER_APPROVED/FINANCE_APPROVED/CANCEL_REQUESTED/CANCELLED 等 8 状态完整流转
  - 撤回：提交后、领导审批前允许一键撤回至草稿
  - 取消：财务终审通过后可发起取消申请，经领导审批同意后作废（逻辑删除）
  - 新增 trip_request 表：支持从工单发起自动关联 / 独立发起填写理由；预留预算字段 est_transport_cost / est_accommodation_cost
  - API：提供 submit/withdraw/cancel-request 等动作接口
  - 强约束联动：ExpenseService 守门员——TRANSPORT/ACCOMMODATION 费用必须关联有效出差申请，否则拒绝保存
- 验收：Tests Passed（TripRequestTest 全绿；全量回归通过）
- 结论：✅ 封板

### 2026-01-30-11（费用 & 报销 简单统计 Expense/Claim Stats MVP）
- 完成内容：
  - 统计聚合：采用 Batch Fetching + 内存聚合，避免 JPA N+1；支持 User/Customer/WorkOrder/Trip 四维切换
  - 归属推导：支持 WorkOrder -> Customer 等跨实体溯源，确保按客户统计准确
  - 指标口径：费用总额 / 已提交 / 已通过 / 作废驳回 四种互斥口径；出差维度额外“预算 vs 实际”差额分析
  - 多币种：原币种分别汇总 + 折算人民币总计（Mock Rate）
  - 导出：集成 EasyExcel，导出 Summary（汇总）+ Detail（明细）双 Sheet
  - 工程治理：测试框架升级到 Spring Boot 3.4+ 标准，@MockBean 全量替换为 @MockitoBean
- 验收：Tests Passed（ExpenseStatsTest 全绿；含聚合正确性、多币种、归属推导、出差预算对比、Excel 导出可用性）
- 结论：✅ 封板

### 2026-01-30-12（回款记录 + 统一核销分摊 Payment & Allocation MVP）
- 完成内容：
  - 回款记录：支持录入回款（单客户、单币种、金额、到账日期），并分摊到多张未完结报价单。
  - 严格配平（无容差）：
    - 回款侧：回款金额必须等于分摊合计（不允许挂账/剩余）
    - 对账单侧：绑定对账单时，绑定回款合计必须等于对账单总额（差一分钱拦截）
  - 报价回款累计：回款确认后累计到报价“已回款金额”，并在满足全额时驱动报价进入“已付清/PAID”。
  - 不可逆与审计：回款确认后锁定，禁止直接修改分摊；只能走冲正/作废类流程，保留审计链。
- 验收：Tests Passed（PaymentTest 全绿）
- 结论：✅ 封板

### 2026-01-30-13（价格/费率表 PriceBook / Contract Pricing MVP）
- 完成内容：
  - 合同价表：支持按合同维护结构化价格/费率（平台费/ICCID/维修/运费等），并支持启用/停用（停用后不再参与建议价）。
  - 建议价策略：
    - 平台费：同客户多合同同时命中时取“最低价优先”。
    - 其他费用：按合同优先级优先（高优覆盖低优），保证商务策略可控且稳定。
  - 停止自动建议死线：报价单发生第一次递交后，永久停止自动建议价/自动刷新，锁定当时价格快照，避免合同变更影响历史报价。
  - 审计留痕：系统建议价记录“命中来源信息”；人工改价必须填写原因并留痕（谁/何时/改前改后/原因）。
  - 权限：仅财务/管理员可维护价表；普通用户仅可引用建议价。
- 验收：Tests Passed（PriceBookTest 全绿）
- 结论：✅ 封板


### 2026-01-30-14（承运商范围规则 + 发货口径推导 + 运费账单合并导出 MVP）
- 完成内容：
  - 承运商字典：承运商必须从字典选择（支持启用/停用），避免手填脏数据。
  - 承运商范围规则：支持配置“到货地址→承运商”覆盖规则；命中才自动带出，未命中强制手选；用户允许手动改选。
  - 发货口径：不新增“发货性质”手填字段；基于粘贴内容/委托字段做类型推导；推导不确定时必须要求人工确认（不瞎猜）。
  - 运费口径绑定发货类型：
    - 客户委托：公司先垫付承运商费用（进入运费账单），同时运费需走报价单向客户收费。
    - 其他类型：走公司经费，不产生向客户收费的运费收入。
  - 运费账单：按“单一承运商”对账；导出支持把多张账单合并为一个文件（方便领导在同一张单子签字）。
- 验收：Tests Passed（相关测试全绿）
- 结论：✅ 封板

### 2026-01-31-01（数据更正单 Correction MVP）
- 完成内容：
  - 更正单能力：新增通用更正记录（支持 JSON 快照），用于记录“更正对象/原因/附件/新旧链路”并可追溯审计。
  - 更正方式：统一采用“作废旧记录 + 生成正确新记录（克隆/重建）”，覆盖 Quote / FreightBill / Payment / Expense 等财务相关对象。
  - 权限与流程：区分“创建更正单”与“审批执行更正”；提交更正单强制附件；执行后形成更正链路（旧→更正单→新）。
  - 稳定性与治理：修复架构分层违规与测试回归问题，保证数据完整性与字典校验一致。
- 验收：Tests Passed（全量测试通过：`.\mvnw.cmd clean test` BUILD SUCCESS；28 模块、50+ 测试套件全绿）
- 结论：✅ 封板

### 2026-01-31-03（Swagger/OpenAPI 集成）
- 完成内容：
  - 对接契约交付：集成 Swagger/OpenAPI 文档，作为“前端对接的唯一接口说明书”，可按模块浏览/搜索全部业务接口。
  - 文档入口：提供 OpenAPI JSON 与 Swagger UI 页面（/v3/api-docs、/swagger-ui.html）。
  - 鉴权与错误契约：
    - 支持 Bearer Token 鉴权（便于联调时直接在 UI 内试调接口）。
    - 统一错误返回契约：`message + machineCode`，并确保文档中可见（便于前端按 machineCode 做提示分支）。
  - 文档质量：核心业务模块（Quote / Shipment / Finance / Correction / MasterData 等）已补齐分组（Tag）、字段说明（Schema）与关键示例（Example）。
- 验收：SwaggerTest 通过（GET /v3/api-docs 200，包含 openapi/paths/components，包含 machineCode）。
- 结论：✅ 封板

### 2026-01-31-04（前端工程 MVP-0）
- 完成内容：
  - 前端工程初始化完成：统一布局/路由/登录退出闭环，形成可扩展的前端骨架。
  - 核心页面可用：Customer / Quote / Shipment / Finance 页面均可正常打开。
  - 表格组件完善：支持刷新、密度切换、列设置等基础生产力功能；空数据提示逻辑正确；控制台无报错，运行流畅。
- 验收：人工验收满分通过（核心页面可用、交互正确、无报错）。
- 结论：✅ 封板

### 2026-01-31-05（前后端深度联调 Integration-1：新建客户 + 新建报价草稿）
- 完成内容：
  - 客户写入闭环：
    - 前端支持新建客户并成功写入数据库；
    - 客户列表可回显新数据（排序/分页正常）；
    - 重复命名拦截生效。
  - 报价写入闭环：
    - 前端支持新建报价草稿（DRAFT），可拉取客户下拉列表；
    - 报价列表（/api/v1/quotes）可回显新建单据。
  - 已知问题（后续优化项，不影响本里程碑封板）：
    - 报价列表客户列存在 fallback 显示（如“客户 #27”），说明关联成功但名称回填待优化（计划在 Integration-2 修复）。
- 验收：人工验证全链路打通（Customer/Quote 创建与列表回显均通过）。
- 结论：✅ 封板

### 2026-01-31-06（报价闭环精修 Integration-2）
- 完成内容：
  - 客户名回填修复：报价列表与报价详情均展示客户公司全称（不再出现“客户 #ID”fallback）。
  - 明细行编辑闭环：在 DRAFT 状态下支持明细行增/删/改；前后端联调顺畅（items 的新增/编辑/删除动作均可用）。
  - 提交与校验：
    - 空明细提交被正确拦截并提示；
    - 有明细时提交成功，报价状态从 DRAFT 流转到 APPROVAL_PENDING（或等价 PENDING）；
    - 提交后详情页进入只读模式，相关操作按钮隐藏，防止继续编辑。
  - 体验说明：提交后返回列表需手动刷新才能看到最新状态，按 MVP 预期接受（非 blocker）。
- 验收：人工验收通过（客户名显示、明细编辑、提交拦截与状态流转、UI 锁定均 Verified）
- 结论：✅ 封板

### 2026-01-31-07（Integration-3：履约与财务 Shipment & Payment）
- 完成内容：
  - 履约闭环：
    - 发货单支持“双入口”：独立创建 + 从报价单创建。
    - 权限修复：解除“仅负责人可发货”的限制并通过验证。
  - 财务闭环：
    - 支持针对报价单录入回款记录。
    - 支持回款“确认（Confirm）”动作（含 Controller 冲突修复）。
    - 自动结清：当回款金额 >= 报价总额时，报价单状态自动流转为 PAID（已结清）。
- 验收：人工全流程验收通过（客户 -> 报价 -> 发货 -> 回款 -> 自动 PAID 全链路 Verified）
- 结论：✅ 封板

### 2026-01-31-08（Integration-4：售后换货/返修 After-Sales MVP）
- 完成内容：
  - 入口管控：严格限制仅能从“发货单”发起售后单，确保来源可追溯。
  - 机器号录入（MVP 变通）：支持机器/序列号手填录入，解决发货单未记录序列号导致的卡点。
  - 业务规则：
    - 附件强制：无附件提交被正确拦截，确保售后有据可依。
    - 状态流转闭环：草稿 → 已提交 → 已收到返件 → 处理中 → 已寄回/已发出 → 已结束，全链路顺畅。
  - 物流衔接：在“已寄回/已发出”阶段前端正确引导创建“退货发货单”，实现逆向物流与正向物流衔接，并可追溯关联。
- 验收：人工验收通过（入口限制、手填机器号、附件拦截、状态流转、退货发货单引导均 Verified）
- 结论：✅ 封板

### 2026-01-31-10（历史数据导入 MVP：七大主数据批量导入）
- 完成内容：
  - 全量覆盖：支持 7 类主数据的 Excel 批量导入（客户、合同、机型、零部件、机器、SIM卡、机型物料清单）。
  - 智能冲突治理：导入时按唯一键比对，不暴力覆盖；自动生成“冲突清单”，支持人工逐条选择【跳过 / 覆盖更新 / 复用引用】。
  - 物料清单专项：
    - 导入物料清单时自动聚合生成“草稿版本”；
    - 自动校验图号存在性，并与“冲突治理中心”逻辑打通。
  - 容错与报告：允许部分成功；失败行输出明确原因（如引用关系不存在），并可导出失败明细报告。
- 验收：人工验收通过（7 类导入可用、冲突清单可处理、物料清单草稿与校验闭环、失败原因可追溯）
- 结论：✅ 封板

### 2026-02-01-01（仓库与库存 MVP）
- 完成内容：
  - 多仓架构：建立“总部大库 + 服务站小库”的数据模型；系统启动自动初始化默认仓库。
  - 库存核心：支持总部大库入库；支持出库（强校验：必须绑定单据类型+单号、必须有库存）；所有库存变动记录库存流水（可追溯）。
  - 补货审批：实现“申请 → 两级审批（一级/二级）→ 自动调拨”全链路；状态流转清晰（草稿 → 待审批 → 已完成）。
  - 前端页面：库存管理页支持筛选/查库存/手动出入库；补货审批页按“我的申请 / 待我审批”分栏展示。
- 验收：Verified with Constraints（带约束通过）
- 已知约束（Backlog）：
  - 入库校验：当前允许非主数据图号入库（后续需加主数据强校验）。
  - 出库入口：当前保留手动出库按钮（后续隐藏，改为由工单/报价单触发）。
  - 补货物流：当前审批通过即瞬间调拨（后续增加“发货/收货”物理节点）。
- 结论：✅ 封板


---

## E. 勘误（如需）
- （无）
