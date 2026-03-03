# BIZPERM-260302-001-04-EVID — 证据收据（只读）

**Request ID:** BIZPERM-260302-001-04-EVID  
**Purpose:** 为 “Handled-only vs All” 范围安全实现收集仓库事实，不做任何代码修改。

---

## Completed?

Yes.

---

## Findings

**Item 1 — Catalog flag semantics**
- `biz_perm_operation_point.handled_only` 定义于 `backend/dfbs-app/src/main/resources/db/migration/V0080__biz_perm_catalog.sql`（`handled_only BOOLEAN NOT NULL DEFAULT false`），实体为 `BizPermOperationPointEntity.java`（`handled_only` 列）。当前用法：目录侧“该操作点是否标记为仅已处理”的元数据；超管在目录维护中可切换（`BizPermCatalogMaintenance.tsx` 表列「仅已处理」、`updateHandledOnly`；`BizPermCatalogController` PUT `/op-points/{id}/handled-only`）。**结论**：作为“支持/标记为仅已处理”的目录属性，**不是**“默认范围”或“当前账号范围”。
- UI 筛选「**只看仅已处理**」：`frontend/dfbs-ui/src/pages/Admin/AccountPermissions/BizPermAssignmentView.tsx` 约 220 行（Checkbox 文案）、约 147 行 `if (filterOnlyHandledOnly) list = list.filter((op) => op.handledOnly)`；使用的字段为 catalog 返回的 **`op.handledOnly`**（来自 `biz_perm_operation_point.handled_only`）。

**Item 2 — Per-account direct assignment storage (Step-03)**
- **保存**：`BizPermAssignmentView.tsx` 约 168–175 行，`doSave()` 内调用 `saveAccountOverride(userId, { roleTemplateId: null, addKeys: Array.from(draftCheckedKeys), removeKeys: [] })`。与文档一致：**roleTemplateId=null，addKeys=勾选 keys，removeKeys=[]**。
- **还原路径（roleTemplateId=null 时）**：同文件约 111–124 行，`useEffect` 依赖 `savedOverride?.userId, savedOverride?.addKeys`；当 `savedOverride` 存在时，用 `savedOverride.addKeys` 初始化 `draftCheckedKeys` 与 `lastLoadedCheckedKeys`。即 **roleTemplateId=null 时，勾选集合完全由 addKeys 还原**；`handleRestore()`（约 185–188 行）将草稿恢复为 `lastLoadedCheckedKeys`。

**Item 3 — Existing “scope” storage / precedent**
- 搜索 backend（scope / dataScope / handledOnlyScope / 参与 / history）：**未发现** per-user 或 per-permission 的 “scope / dataScope / handledOnlyScope” 存储或 API。`scope`、`history` 仅出现在报价/BOM/SimCard 等业务（如 quote workflow history、bom history），与权限/账号范围无关。

**Item 4 — Best place to store per-account op scope**
- 现有相关表：`perm_user_permission_override`（V0074）唯一约束 **uk (user_id, permission_key, op)**，op 为 ADD/REMOVE；用于“追加/移除”权限，无 scope 字段。
- **建议**：新增**独立表**存储“按账号·按权限键”的范围，例如 **`biz_perm_user_op_scope`**：`(user_id BIGINT, permission_key VARCHAR(128), scope VARCHAR(32))`，唯一约束 **(user_id, permission_key)**，scope 如 `'ALL' | 'HANDLED_ONLY'`。理由：(1) override 表语义为 add/remove，同一 (user_id, permission_key) 可同时存在 ADD 与 REMOVE，不适合再塞 scope；(2) 单独表语义清晰，扩展方便；(3) 不与现有 uk 冲突；(4) 若将来与 override 联合查询，按 user_id 关联即可。

**Item 5 — Gating requirements**
- **Admin-only 页面**：`/admin/account-permissions` 路由由 **AdminOrSuperAdminGuard** 保护（`App.tsx` 约 107 行）；后端 `AccountPermissionsController` 使用 **AdminOrSuperAdminGuard**（`adminGuard.requireAdminOrSuperAdmin()`），路径前缀 `/api/v1/admin/account-permissions`。
- **Allowlist-only 目录维护**：`BizPermCatalogController` 中 GET `/catalog` 及所有 nodes/op-points 写接口使用 **PermSuperAdminGuard**（`requirePermSuperAdmin()`）；GET `/catalog/read` 使用 **AdminOrSuperAdminGuard**（admin 只读）。
- **新 scope 设置 API**：应使用 **AdminOrSuperAdminGuard**（admin-only），**不要**使用 allowlist-only（PermSuperAdminGuard），以便普通 admin 在按账号分配流程中为账号设置操作范围，与 Step-03 的“admin 可写 override”一致。门控实现位置：后端新 endpoint（如 under account-permissions 或 bizperm）加 `AdminOrSuperAdminGuard`；前端已在 account-permissions 页内，由同一路由守卫覆盖。

---

## Not found items

- 无 per-user / per-permission 的 scope / dataScope / handledOnlyScope 存储或 API（Item 3）。

---

## Full-suite build

Not executed（证据只读，未改代码）。

---

## Blocker question

None.
