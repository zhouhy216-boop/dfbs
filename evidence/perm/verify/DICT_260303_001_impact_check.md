# DICT-260303-001 影响检查（事实仅列）

**Request ID:** DICT-260303-001-IMP  
**Related:** DICT-260303-001 / Step 01（系统 → 数据字典，仅超管）

---

## 1) 可能影响范围（简述）

- **页面/流程**
  - 系统菜单下现有：数据确认中心、平台配置（基础）；超管额外见：层级配置、组织架构、变更记录、**字典类型**、历史显示示例（`BasicLayout.tsx` 第 99–106 行，`ORG_STRUCTURE_MENU`）。
  - 若新增「数据字典」入口：需在 `BasicLayout` 的 `admin-group` 或 `ORG_STRUCTURE_MENU` 中增加一项（或与「字典类型」并列/替换），并保证仅 `isSuperAdmin` 时展示。
  - 路由：`App.tsx` 第 104–106 行已有 `SuperAdminGuard` 包裹的 `/admin/dictionary-types`、`/admin/dictionary-types/:typeId/items`、`/admin/dictionary-snapshot-demo`。
- **模块/文件**
  - 前端：`frontend/dfbs-ui/src/layouts/BasicLayout.tsx`（菜单构建、`buildMenuRoutes`、`ORG_STRUCTURE_MENU`）；`frontend/dfbs-ui/src/App.tsx`（路由与 SuperAdminGuard）；若新页：`pages/Admin/` 下新页面或沿用 `DictionaryTypes`/`DictionaryItems` 所在目录；`features/dicttype/`（dictType、dictItem、dictRead、dictSnapshotDemo 等）。
  - 后端：`DictionaryTypeAdminController`、`DictionaryItemAdminController`、`DictionarySnapshotDemoController` 均已使用 `SuperAdminGuard`（`/api/v1/admin/dictionary-types`、`/api/v1/admin/dictionary-items`、`/api/v1/admin/dictionary-snapshot-demo`）；只读接口 `/api/v1/dictionaries/{typeCode}/items` 无 admin 路径，被报价等业务使用。
- **API/契约**
  - 现有字典管理接口均为超管门控；若 Step-01 仅新增菜单/入口或聚合页且不新增后端接口，则无契约变更。若新增聚合接口（如「数据字典概览」），需同样使用 `SuperAdminGuard` 与现有门控一致。

---

## 2) 回归关注点（建议必测）

- **现有行为**
  - 非超管不显示「字典类型」「历史显示示例」等 ORG_STRUCTURE_MENU 项；超管可见且可访问 `/admin/dictionary-types` 及字典项子页；直接访问 URL 时 `SuperAdminGuard` 会拦截非超管并重定向。
  - 报价等使用 `getDictionaryItems(typeCode)` 调用只读 `/v1/dictionaries/{typeCode}/items`，无 SuperAdmin 要求，不得因本次改动被破坏。
  - `request.ts` 中对 `dictionary-types`、`dictionary-items` 的 400/409 不自动弹 message，由页面展示中文错误；此逻辑不宜改动。
- **关键路径**
  - 超管：系统 → 字典类型 → 列表/新建/编辑/禁用/删除类型 → 进入某类型字典项 → 增删改查/排序。
  - 非超管：不应看到新「数据字典」入口（若与字典类型并列或替换，需确认原「字典类型」入口的可见性设计）；调用 admin 字典接口应保持 403。
  - 业务侧：报价等依赖只读字典接口的页面加载与选项展示正常。

---

## 3) 构建/测试状态

- **全量构建**：未执行（仅做影响梳理）。
- **失败用例**：未运行测试，无列表。

---

*最大约 30 行；无重构建议、无范围扩大。*
