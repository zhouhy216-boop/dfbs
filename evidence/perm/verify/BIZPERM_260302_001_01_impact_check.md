# BIZPERM-260302-001-01 影响评估：业务模块视图骨架（仅事实）

**Request:** BIZPERM-260302-001-01-IMP  
**Scope:** 实施前事实与回归清单，无代码修改。

---

## 1) 可能影响范围

**Pages/flows**

- **入口路由**：`/admin/account-permissions`（`App.tsx` 中 `Route path="admin/account-permissions"`，由 `AdminOrSuperAdminGuard` 包裹）。
- **账号编辑触发**：在「账号与权限」→ **账号** Tab 内，点击表格行或「详情」按钮 → `setSelectedUser(record)` + `setAccountDetailDrawerOpen(true)`（`AccountsTab.tsx` 约 474、493 行）；编辑为 **Drawer**（非 Modal），`open={accountDetailDrawerOpen && selectedUser != null}`，约 588–838 行。
- **骨架注入位置**：Drawer 内当前顺序：① 账号信息块 ② 启用/重置密码 ③（无权限时）提示 ④（有权限时）保存/还原 + 分配角色模板 + 差异视图 + 高级编辑（Collapse）。**业务模块视图骨架**适合放在 ① 之后、④ 之前（与权限区并列），或 ④ 内、差异视图之前/之后；具体以产品为准，不改动现有保存/还原与差异视图 DOM 顺序即可避免回归。

**Modules/files**

- **前端**：`frontend/dfbs-ui/src/pages/Admin/AccountPermissions/AccountsTab.tsx`（账号列表 + 账号详情 Drawer）；`AccountPermissions/index.tsx`（Tabs 容器）；`App.tsx`（路由）；`BasicLayout.tsx`（菜单「账号与权限」由 `isAdminOrSuperAdmin` 控制）；`shared/components/AdminOrSuperAdminGuard.tsx`（路由级门控）。
- **门控**：整页 `/admin/account-permissions` 由 `AdminOrSuperAdminGuard` 包裹（非 admin 无法进入）；账号编辑在 Tab 内，无额外门控；Drawer 内「无权限管理角色/权限」由 `treeUnavailable`（权限树 403）控制，与 admin 门控独立。

**APIs/contracts**

- **打开账号详情时**：`getAccountOverride(selectedUser.id)`（`AccountsTab` 内 `useEffect` 依赖 `selectedUser?.id`）；模板变更时拉取 `getRolePermissions(draftRoleTemplateId)`；权限树 `fetchPermissionTree()`（allowlist）。
- **保存覆盖**：`saveAccountOverride(selectedUser.id, { roleTemplateId, addKeys, removeKeys })`（`handleSave`）。
- **结论**：骨架占位无需新接口；仅新增 UI 区块即可。

---

## 2) 回归清单（必测）

- **Admin-only**：非 admin 不应看到「账号与权限」菜单项且访问 `/admin/account-permissions` 被重定向或 403；账号编辑仅在进入该页后可用，无额外暴露。
- **按账号覆盖**：差异视图、保存/还原、追加/移除、移除优先、高级编辑折叠、最终生效抽屉等逻辑与状态不变；骨架不依赖 override 的 draft/saved 状态，不改写 `handleSave`/`handleReset` 或 override 相关 DOM。
- **无权限泄漏**：刷新/登出/登录后 effective-keys 与缓存行为不变；本步不新增接口或权限判断。
- **Super-admin 仅维护**：本步仅增加前端骨架，**不**新增可写的“业务模块目录”接口；若后续有“仅 super-admin 维护的业务模块目录”，将挂接在独立入口/接口上，本步不实现、不暴露。

---

## 3) Build/test status

未执行。可后续在 `backend/dfbs-app` 运行 `.\mvnw.cmd -q -DskipTests compile`，在 `frontend/dfbs-ui` 运行 `npm run build` 验证（前端可能受既有 TS 错误影响）。
