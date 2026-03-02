# BIZPERM-260302-001-02 证据收集回执（只读，未修改任何代码）

**Request:** BIZPERM-260302-001-02-EVID  
**Scope:** 业务模块目录相关事实：权限键来源、左侧菜单来源、可复用持久化/审计、骨架插入点与超管判断。

---

## Completed? Yes

---

## Item 1 — Permission key universe (source of truth)

- **DB 表**：`perm_action`（V0071，种子 10 个 action_key：VIEW/CREATE/EDIT/SUBMIT/APPROVE/REJECT/ASSIGN/CLOSE/DELETE/EXPORT）；`perm_module`（id, module_key, label, parent_id, enabled）；`perm_module_action`（module_id, action_key，限定某模块可用动作）。角色/覆盖：`perm_role_permission`（role_id, permission_key）、`perm_user_role_template`（user_id, role_id）、`perm_user_permission_override`（user_id, permission_key, op ADD|REMOVE）（V0072/V0074）。
- **枚举方式**：有效权限键 = 所有 `module_key:action_key`，其中 module 来自 `perm_module`，action 来自该 module 的 `perm_module_action`（或 fallback 用 perm_action 全集）。**唯一返回“完整集合”的 API**：`GET /api/v1/admin/perm/permission-tree`（allowlist）；响应 `PermissionTreeResponse` 含 `keyFormat`、`actions`（ActionItem[]）、`modules`（树，每节点 key/label/actions[]/children）；扁平化树中每个节点的 `key` × 该节点 `actions[]` 即全部 permission key。实现：`PermPermissionTreeService.getPermissionTree()`（`application/perm/PermPermissionTreeService.java`），数据来自 `PermActionRepo`、`PermModuleRepo`、`PermModuleActionRepo`。
- **数量与示例**：总个数取决于 DB 中 `perm_module` 与 `perm_module_action`；默认迁移不插入模块，故可为 0。若启用 Dev 种子（`DevPermDemoModulesSeeder`）：`platform_application`、`platform_application.orders`（子节点 actions VIEW/CREATE/EDIT），则可得如 `platform_application:…`、`platform_application.orders:VIEW` 等。菜单门控用到的键见 BasicLayout：`platform_application.orgs:VIEW`、`platform_application.applications:VIEW`、`work_order:VIEW`（常量 PERM_ORGS_VIEW 等）。**10 个示例（action 级）**：VIEW, CREATE, EDIT, SUBMIT, APPROVE, REJECT, ASSIGN, CLOSE, DELETE, EXPORT（来自 V0071 或 `PermPermissionTreeService.FALLBACK_ACTIONS`）；**完整 key 示例**（依赖现有模块）：e.g. `work_order:VIEW`、`platform_application.orgs:VIEW`、`platform_application.applications:VIEW`（若对应模块在 perm_module 中存在）。

---

## Item 2 — Left menu structure source (to match CN tree)

- **前端文件**：`frontend/dfbs-ui/src/layouts/BasicLayout.tsx`。菜单树由静态 `MENU_ROUTES_BASE`（约 38–97 行）定义，经 `buildMenuRoutes(isSuperAdmin, permAllowed, isAdminOrSuperAdmin, hasPermission)`（约 116–147 行）过滤后使用。
- **与 perm_module 关系**：**菜单树 ≠ perm_module 树**；二者独立。菜单是前端写死的路由/名称（中文）；仅部分菜单项按 permission key 做可见性过滤（platform-group 用 PERM_ORGS_VIEW/PERM_APPS_VIEW；after-sales-service-group 用 PERM_WORK_ORDER_VIEW）。perm_module 树来自 DB，用于权限配置/树展示与 key 校验，不驱动左侧菜单结构。
- **菜单层级摘要（1–2 层）**：Dashboard、报价单、物流管理（发货列表、运输异常）、售后服务（工单管理）、财务、库存管理、补货审批、数据导入、主数据（客户/合同/机器/型号BOM/零部件/SIM卡）、平台&网卡管理（平台管理/SIM管理/申请管理）、系统（数据确认中心、平台配置 + 动态追加 账号与权限/角色与权限/层级配置等）。

---

## Item 3 — Existing persistence patterns to reuse

- **已有**：`app_setting` 表（V0078，key/value/updated_by_user_id/updated_at），用于如 auth.defaultPassword；实体 `AppSettingEntity`、`AppSettingRepo`。`perm_audit_log` + `PermAuditService`（`ACTION_*` 常量）：ROLE_TEMPLATE_SAVE、ACCOUNT_OVERRIDE_SAVE、MODULE_CREATE/UPDATE/DELETE、MODULE_ACTIONS_SET、ACCOUNT_ENABLE_SET、ACCOUNT_PASSWORD_RESET、ROLE_TEMPLATE_CLONE、DEFAULT_PASSWORD_CHANGED 等；`log(actionType, targetType, targetId, targetKey, note)`，REQUIRES_NEW 写入。组织/配置侧：`org_position_catalog`（order_index）、`WarehouseConfigEntity`、层级/顺序类 order_index 更新逻辑（OrgLevelService 等）。
- **不存在**：无专门“业务模块目录”表或“菜单树顺序/目录”表；无与 perm_module 平行的“业务模块 catalog”持久化。目录维护类操作尚无单独 audit action（可复用 MODULE_* 或新增类似常量）。

---

## Item 4 — UI insertion point for catalog maintenance

- **骨架位置**：`frontend/dfbs-ui/src/pages/Admin/AccountPermissions/AccountsTab.tsx`，约 621–658 行：在账号详情 **Drawer** 内，位于「账号信息」块与「启用/重置密码」之后、`treeUnavailable` 提示及权限区（保存/还原、分配角色模板、差异视图、高级编辑）之前；区块标题「业务模块视图」，三张占位卡片 + 底部「维护目录」占位与说明文案。
- **超管判断**：`useIsPermSuperAdmin()`（`@/shared/components/PermSuperAdminGuard`），返回 `{ allowed, loading }`；allowed 来自 `GET /v1/admin/perm/super-admin/me` 的 `allowed` 或本地 `permAllowedCache`。AccountsTab 内 `const { allowed: permSuperAdminAllowed } = useIsPermSuperAdmin();`（约 54 行）；仅当 `permSuperAdminAllowed === true` 时渲染「维护目录（仅超管）」禁用按钮，否则只显示「目录维护仅超管可用（后续）」。

---

## Not found items

- 无现成 API 返回“业务模块目录”或“菜单项与 permission key 的映射表”；无 to-claim/unclassified 等业务状态字段在权限/菜单代码中的使用。

---

## Full-suite build

未执行。可后续在 backend 运行 `.\mvnw.cmd -q -DskipTests compile`，在 frontend 运行 `npm run build` 验证。

---

## Blocker question

无。
