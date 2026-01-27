# PROJECT_STATUS.md（DFBS 唯一推进状态文件｜v2.1_final｜四人共读版）

用途：
- 本文件是 DFBS 项目的【唯一推进状态源】与【封板记录】。
- 仅记录：已冻结规则、当前唯一推进点（状态）、里程碑流水账（只追加）。
- 不记录：对话话术、个人待办清单、排期计划、代码全文细节（代码细节在工单/提交里）。
## 全局规则（必须遵守）：
- 里程碑只允许【追加】，不允许回头改历史（发现错误用“勘误”追加说明）。
- 同一天多条里程碑，用序号区分：`YYYY-MM-DD-01 / 02 / 03 ...`
- 每次 User 验收通过（Tests Passed），必须追加一条里程碑并标记“✅ 封板”。
- 从现在开始：更新只做两件事
  1) 替换【C. 当前唯一推进点】的两行
  2) 在【E. 里程碑流水账】末尾追加一条
- 当前只允许 1 个“唯一推进点”。除该推进点以外的想法/需求，一律写入 PROJECT_PARKING_LOT.md（只追加，不进入开发）。
- 只有当 PM 明确把某条 Parking Lot 选为新的“唯一推进点”，并完成 Interview → Logic Freeze 后，才允许进入 Dev&Test。
- 除非 CEO 明确说 “Tests Passed”，否则任何人不得更新 PROJECT_STATUS.md 的里程碑与推进点。


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

## C
- 当前唯一推进点：发件报价单抄送仓库（Quote CC Warehouse）MVP  
- 状态：🚧 进行中（里程碑ID：2026-01-27-02）

> 说明：这里永远只有两行；下一推进点由 PM 在对话中启动新一轮 Interview 后确定，然后只替换这两行。

---

## D. 客观进度快照（只写事实，不写计划）

### D1. 主数据（MasterData）
- 已存在主数据实体/表：Customer / Contract / Product / Machine / ICCID
- 已封板能力（以里程碑为准）：
  - Create 最小闭环：Customer / Contract / Product / Machine / ICCID ✅
  - 列表 + 关键字搜索：Customer / Contract / Product ✅

### D2. 报价（Quote）
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

## E. 里程碑流水账（Append Only）

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
---

## F. 勘误（如需）
- （无）
