# ACCTPERM Step-05.c 证据：账号覆盖 diff-only 是否需后端额外只读数据

**Ticket:** ACCTPERM-260211-001-05.c  
**结论：当前数据充足，无需新增后端接口。**

---

## 1) 现有数据与前端可推导性

**GET /api/v1/admin/account-permissions/accounts/{userId}/override**（已存在，AdminOrSuperAdminGuard）  
- 返回：`userId`, `roleTemplateId`, `roleTemplateKey`, `addKeys`, `removeKeys`, `effectiveKeys`  
- **未返回**：`roleTemplateLabel`、`inheritedKeys`（模板继承的权限键列表）

**GET /api/v1/admin/account-permissions/roles/{id}/permissions**（已存在，同命名空间，AdminOrSuperAdminGuard）  
- 返回：`permissionKeys: string[]`（该角色的权限键）  
- **鉴权**：与 override 相同，仅要求 Admin 或 SuperAdmin，**不要求** allowlist / 权限树超级管理员

**GET /api/v1/admin/account-permissions/roles**（已存在）  
- 返回角色列表，含 `id`, `roleKey`, `label` 等  
- 前端用 `roleTemplateId` 在列表中查找即可得到 **模板名称（label）**

---

## 2) Allowlist 超级管理员

- **templateKeys（继承）**：前端对当前账号的 `roleTemplateId` 调 `getRolePermissions(roleTemplateId)` 即可得到继承权限键，**不依赖** allowlist。  
- **模块/动作标签**：有权限树时由 `GET /api/v1/admin/perm/permission-tree` 得到，用于分组与展示；无权限树时用 `moduleKey`（及 key 中的 action 部分）即可，无歧义。

---

## 3) 非 Allowlist 管理员

- **继承**：同上，`getRolePermissions(roleTemplateId)` 在 account-permissions 命名空间下，**仅需 Admin/SuperAdmin**，非 allowlist 管理员也可调用，故可得到 templateKeys 与继承数量。  
- **模板名称**：来自 `getRoles()` 的列表 + `roleTemplateId`，无需 override 接口再返回 label。  
- **addKeys / removeKeys / effectiveKeys**：GET override 已提供，足够做「追加 / 移除 / 最终生效」的差异展示。  
- **模块标签**：无权限树时无模块中文名，用 `moduleKey`（如 `work_order`）作为分组标题即可，可读且无歧义。

---

## 4) 是否存在“缺失数据”导致无法做 diff-only

| 数据项           | 来源                         | 是否满足 diff-only |
|------------------|------------------------------|--------------------|
| 继承权限键       | getRolePermissions(roleId)   | 是                 |
| 继承数量         | 上者 length                  | 是                 |
| 模板名称         | getRoles() + roleTemplateId  | 是                 |
| 追加/移除/最终   | GET override                 | 是                 |
| 模块中文名       | 权限树（仅 allowlist）        | 非必须；无则用 key |

**结论**：无硬性缺失；无需 `inheritedKeys` 或 `roleTemplateLabel` 从 override 接口再返回，也无需新接口。

---

## 5) 是否新增 GET .../override-diff

**不需要。**  
- 前端已可通过「GET override + getRolePermissions(roleTemplateId) + getRoles()」得到：继承、追加、移除、最终、模板名称与继承数量。  
- 非 allowlist 仅缺模块中文名，用 moduleKey 展示即可，不构成必须后端支持的 blocker。

---

**Completed:** Yes  
**Evidence:** 当前数据与现有 admin 接口足够支撑「inherited vs overrides vs effective」的 diff-only 展示；无需新增只读接口。  
**Endpoint added:** None.  
**Blocker:** None.
