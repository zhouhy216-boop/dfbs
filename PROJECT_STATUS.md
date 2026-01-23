# PROJECT_STATUS.md（DFBS 唯一推进状态文件｜v2.1_final｜四人共读版）

用途：
- 本文件是 DFBS 项目的【唯一推进状态源】与【封板记录】。
- 仅记录：已冻结规则、当前唯一推进点（状态）、里程碑流水账（只追加）。
- 不记录：对话话术、个人待办清单、排期计划、代码全文细节（代码细节在工单/提交里）。

更新规则（强约束）：
- 里程碑只允许【追加】，不允许回头改历史（发现错误用“勘误”追加说明）。
- 同一天多条里程碑，用序号区分：`YYYY-MM-DD-01 / 02 / 03 ...`
- 每次 User 验收通过（Tests Passed），必须追加一条里程碑并标记“✅ 封板”。
- 从现在开始：更新只做两件事
  1) 替换【C. 当前唯一推进点】的两行
  2) 在【E. 里程碑流水账】末尾追加一条

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

## C. 当前唯一推进点（Only One）

- 当前唯一推进点：报价单 QuoteItem 明细最小闭环（MVP）
- 状态：✅ 封板（里程碑ID：2026-01-23-06）

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

---

## F. 勘误（如需）
- （无）
