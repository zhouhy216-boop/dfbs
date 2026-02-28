# ACCTPERM-260211-002-04 证据性差距检查（仅事实，无代码修改）

**Ticket:** ACCTPERM-260211-002-04.a  
**Goal:** 判断 Step-002-04 是否已由现有 ACCTPERM-001 工作满足；列出仍存在的差距（如有）。  
**Scope:** 证据收集，不修改代码。

---

## Completed? Yes

---

## 1) 模块权限管理（admin-only，allowlist 门控）

| 要求 | 现状 | 位置/端点 |
|------|------|-----------|
| 模块可见性启用/停用（enable/disable module visibility） | **未实现** | `PermModuleEntity`（`modules/perm/PermModuleEntity.java`）仅有 id, moduleKey, label, parentId；**无** enabled/visibility 字段。perm_module 表无对应列。 |
| 按模块启用/停用动作（set module actions） | **已实现** | 后端 `PermAdminController`：`PUT /api/v1/admin/perm/modules/{id}/actions`（`SetModuleActionsRequest`）；前端 `PermissionTreeTab.tsx`「设置动作」弹窗，勾选动作后保存。 |
| 模块 CRUD（创建/更新/删除） | **已实现** | 后端 `PermAdminController`：`POST /api/v1/admin/perm/modules`、`PUT /api/v1/admin/perm/modules/{id}`、`DELETE /api/v1/admin/perm/modules/{id}`；前端 `PermissionTreeTab.tsx`：新建模块、可编辑模块表格（编辑/删除/设置动作）。 |
| 模块管理 UI 所在位置 | **已实现** | 「系统 → 账号与权限」下 **「权限树」** Tab（allowlist-only，`useIsPermSuperAdmin().allowed`）；非 allowlist 不显示该 Tab。 |
| 使用的端点 | — | `GET /api/v1/admin/perm/permission-tree`（树只读）；`POST/PUT/DELETE /api/v1/admin/perm/modules`；`PUT /api/v1/admin/perm/modules/{id}/actions`。均为 allowlist 门控（PermAdminController）。 |

**说明**：Permission Tree tab 内「可编辑模块」仅列出**本页本次会话中创建的模块**（editableModules 状态），因 GET permission-tree 不返回模块 DB id，既有模块无法在当页做编辑/删除/设置动作，仅能查看树。

---

## 2) 按账号覆盖（per-account overrides）UI

| 要求 | 现状 | 位置 |
|------|------|------|
| 继承 vs 覆盖的清晰展示（差异视图） | **已实现** | ACCTPERM-001 Step-05：继承自模板、追加权限、移除权限、最终生效；仅展示变更项。 |
| 添加/移除、移除优先、保存后生效 | **已实现** | `AccountsTab.tsx`：添加权限/移除权限选择器，draftAddKeys/draftRemoveKeys，effective = (template ∪ add) \ remove；保存/还原。 |
| 从账号列表抽屉进入 | **已实现** | 002-01：账号 Tab 为列表 + 点击行打开详情抽屉；抽屉内含账号信息与权限覆盖编辑（差异视图、添加/移除、高级编辑折叠）。 |

---

## 3) 命名/位置与 002-04 对应关系

| 项目 | 当前实现 | 002-04 可能表述 |
|------|----------|----------------|
| Tab 名称 | **权限树** | “Permission Management” / 权限管理 |
| Tab 内容 | 权限树只读展示 + 模块 CRUD + 设置动作（对“本页创建”的模块） | 模块权限管理 |

- **结论**：当前无名为「Permission Management」的 Tab；**「权限树」** Tab 即模块管理入口（allowlist-only），内容上对应“权限树 + 模块管理”。若 002-04 要求独立「权限管理」命名或菜单位置，属命名/布局差异；功能上模块 CRUD 与 set actions 已存在。

---

## Evidence list（已满足的需求）

- **模块 CRUD**：POST/PUT/DELETE `/api/v1/admin/perm/modules`，后端 `PermAdminController`、`PermModuleManagementService`；前端 `PermissionTreeTab.tsx`、`permService.ts`（createModule, updateModule, deleteModule）。
- **按模块设置动作**：PUT `/api/v1/admin/perm/modules/{id}/actions`，前端「设置动作」弹窗，setModuleActions。
- **权限树只读**：GET `/api/v1/admin/perm/permission-tree`，Permission Tree tab 内展示树。
- **模块管理 UI 门控**：权限树 Tab 仅 allowlist 可见（`index.tsx` 中 `permAllowed ? [{ key: 'tree', label: '权限树', ... }] : []`）。
- **按账号覆盖**：继承 vs 覆盖、差异视图、添加/移除、移除优先、保存后生效，均在 AccountsTab；从账号列表抽屉可进入同一套覆盖编辑。

---

## Gaps（为声称 002-04 Done 仍缺少或需澄清的部分）

1. **模块可见性启用/停用**：当前**无**“模块级启用/停用可见性”概念。perm_module 无 enabled 或 visibility 字段；若 002-04 要求“可启用/停用某模块在权限树或菜单中的可见性”，需新增数据模型与 API/UI。
2. **权限树 Tab 内可编辑范围**：仅对本页会话中**新建**的模块可编辑/删除/设置动作（editableModules）；GET permission-tree 返回的树节点无 DB id，既有模块无法在当页做编辑。若 002-04 要求“对所有模块可编辑”，需后端在 permission-tree 或单独接口中返回模块 id，或前端改用按 moduleKey 的编辑入口。
3. **命名**：若 002-04 明确要求 Tab 名为「权限管理」或 “Permission Management”，当前为「权限树」，需改文案或增加别名。

---

## Build status

未要求（证据仅检查）。

---

## Blocker question

无。若 002-04 不要求“模块可见性启用/停用”且接受“仅对本页创建的模块可编辑”的现状，则大部分能力已由现有实现覆盖；差距仅限上述两点及命名偏好。
