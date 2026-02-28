# ACCTPERM-260211-002-02 证据性差距检查（仅事实，无代码修改）

**Ticket:** ACCTPERM-260211-002-02.a  
**Goal:** 判断 Step-002-02 是否已由现有 ACCTPERM-260211-001 工作满足；列出仍存在的差距（如有）。  
**Scope:** 证据收集，不修改代码。

---

## Completed? Yes

---

## 1) 角色模板 UX（admin-only）— 已满足

| 要求 | 现状 | 位置/端点 |
|------|------|-----------|
| 创建角色仅需中文名称 + 可选描述（无需手动输入 roleKey） | 已实现 | 前端 `RoleTemplatesTab.tsx`：`CreateRoleModal` 仅含名称、描述、启用；`handleCreateRole` 调用 `createRole({ label, description, enabled })` 不传 roleKey。后端 `AccountPermissionsController` POST `/roles`、`PermRoleService.create` 支持 roleKey 可选并自动生成。 |
| roleKey 自动生成、默认隐藏、不可编辑；可选调试开关展示 | 已实现 | `RoleTemplatesTab.tsx`：`showRoleKey` 状态，复选框「显示内部Key」默认不勾选；列表中仅当 `showRoleKey` 为 true 时渲染 `r.roleKey`。创建/编辑流程无 roleKey 输入框。 |
| 支持启用/停用 | 已实现 | 右侧详情区 `draftEnabled` + `Switch`；保存时 `saveRoleTemplate(..., draftEnabled, ...)`。 |
| 支持克隆/复制（新建「-副本」，enabled=false） | 已实现 | 前端 `cloneRole(id)`；后端 `PermRoleService.clone` 新建 label+"-副本"、enabled=false、复制描述与权限、roleKey 自动生成。端点：POST `/api/v1/admin/account-permissions/roles/{id}/clone`。 |
| 保存后生效（label/description/enabled/permissions）；还原恢复上次保存 | 已实现 | `handleSave` 调用 `saveRoleTemplate(selectedRoleId, draftLabel, draftEnabled, permissionKeys, desc)`；`handleReset` 将 draft* 恢复为 saved*。端点：PUT `/api/v1/admin/account-permissions/roles/{id}/template`。 |

---

## 2) 权限分配 — 已满足

| 要求 | 现状 | 位置/端点 |
|------|------|-----------|
| 模块概览快捷操作（只读/读写/全选/清空）或等价能力 | 已实现 | `ModuleOverviewCards.tsx`：每个根级模块卡片四个按钮「只读」「读写」「全选」「清空」；`handleQuickOpRole(node, op)` 使用 `getKeysForOp(node, op)`（`permissionQuickOps.ts` 中 QuickOp: readonly / readwrite / all / clear）。仅改草稿，保存由父组件统一提交。 |
| 菜单树复选框：勾选⇒至少 VIEW；取消⇒清空子树 | 已实现 | `RoleTemplatesTab.tsx`：`getMenuCheckedRole`、`onMenuCheckRole`；勾选用 `getKeysForOp(node,'readonly')` 为子树设 VIEW；取消勾选清空子树全部权限。`PermissionMenuTree.tsx` 提供菜单级复选框 + 子树联动。 |
| 高级动作默认折叠 | 已实现 | `PermissionMenuTree.tsx`：`splitActions(node.actions)` 分出 basic / advanced；基础动作常显，其余放入「更多动作」折叠区。`permissionQuickOps.ts`：`BASIC_ACTION_KEYS`、`splitActions`。 |
| 使用的端点 | — | 角色：GET/POST/PUT/DELETE `/api/v1/admin/account-permissions/roles`；PUT `/api/v1/admin/account-permissions/roles/{id}/template`；POST `/api/v1/admin/account-permissions/roles/{id}/clone`；GET `/api/v1/admin/account-permissions/roles/{id}/permissions`。权限树（allowlist）：前端 `permService.fetchPermissionTree()` → GET `/api/v1/admin/perm/permission-tree`（PermAdminController，allowlist 门控）。 |
| 非 allowlist 回退 | 已实现 | `RoleTemplatesTab`：`permAllowed` 为 false 或 403 时 `treeUnavailable=true`，不请求 permission-tree；展示「无权限查看权限树（需超级管理员）。仅可修改显示名称与启用状态。」不渲染模块卡片与菜单树；仍可修改名称、描述、启用及保存（不清空已有权限键）。 |

---

## 3) 入口与 Tab 命名 — 现状与 002-02 表述差异

| 项目 | 当前实现 | 002-02 表述（如需严格对齐） |
|------|----------|-----------------------------|
| 主入口菜单 | 「系统」→「**账号与权限**」 | 「System → Account & **Permission Management**」 |
| 页面标题 | 「账号与权限」 | 同上（中/英） |
| Tab 1 | **账号** | Account **List** |
| Tab 2 | **角色模板** | Role Templates ✓ |
| Tab 3 | **权限树** | Permission **Management** |

- 代码位置：`BasicLayout.tsx` 中 `ACCOUNT_PERMISSIONS_MENU` 的 `name: '账号与权限'`；`AccountPermissions/index.tsx` 的 `PageContainer title="账号与权限"` 及 `tabItems` 的 label：`账号`、`角色模板`、`权限树`。
- 功能上：第一个 Tab 已是账号列表+详情编辑（002-01）；第三个 Tab 是权限树管理（模块/动作 CRUD）。若 002-02 仅要求「角色模板 UX + 权限分配」能力，则**功能已满足**；若还要求**入口与 Tab 的英文/中文命名**与「Account & Permission Management」「Account List」「Permission Management」完全一致，则存在**命名层面的差距**（需在菜单/标题/Tab 上增加或改为上述文案）。

---

## Evidence list（已满足的需求）

- **角色模板 UX**：创建仅名称+描述、roleKey 自动且默认隐藏、启用/停用、克隆（-副本、enabled=false）、保存后生效与还原；实现于 `RoleTemplatesTab.tsx`、`acctPermService.ts`、后端 `AccountPermissionsController`、`PermRoleService`。
- **权限分配**：模块概览快捷操作（只读/读写/全选/清空）、菜单树复选框（勾选=VIEW 子树/取消=清空子树）、高级动作在「更多动作」中折叠；实现于 `ModuleOverviewCards.tsx`、`PermissionMenuTree.tsx`、`permissionQuickOps.ts`。
- **端点**：admin-only 使用 `/api/v1/admin/account-permissions/roles`、`/roles/{id}/template`、`/roles/{id}/clone`、`/roles/{id}/permissions`；权限树为 allowlist 的 `/api/v1/admin/perm/permission-tree`；非 allowlist 下仍可编辑角色名称/描述/启用并保存，不暴露权限树。

---

## Gaps（为声称 002-02 Done 仍缺少的部分）

1. **命名（可选）**：若产品要求主入口与 Tab 必须为「Account & Permission Management」「Account List」「Role Templates」「Permission Management」（或对应中文「账号与权限管理」「账号列表」「角色模板」「权限管理」），则需在 `BasicLayout.tsx`、`AccountPermissions/index.tsx` 中增加或调整上述文案；当前为「账号与权限」「账号」「角色模板」「权限树」。
2. **功能差距**：无。角色模板 UX 与权限分配（含快捷操作、菜单树、高级动作折叠、allowlist 权限树与非 allowlist 回退）均已由 ACCTPERM-001（Step-03/04）及现有「账号与权限」角色模板 Tab 实现。

---

## Build status

未要求（证据仅检查，未执行构建）。

---

## Blocker question

无。若 002-02 不强制「Permission Management」与「Account List」等措辞，仅要求能力与入口一致，则可视为已由现有工作满足。
