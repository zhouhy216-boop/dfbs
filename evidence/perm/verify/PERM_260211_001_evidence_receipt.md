# PERM-260211-001 证据收集回执（只读，未修改任何代码）

**Request:** PERM-260211-001 evidence only.  
**Scope:** 三项事实：系统内已定义模块列表；账号管理 UI 及账号创建/角色分配/权限设置；权限树结构与模块/动作定义。

---

## Completed? Yes

---

## Findings (bullet list)

**Item 1 — 系统内已定义模块**

- **数据来源**：`perm_module` 表（`backend/.../db/migration/V0071__perm_definition_v1.sql`）；模块由数据/种子动态维护，迁移本身不插入模块行。
- **开发种子（可选）**：`DevPermDemoModulesSeeder.java`（`backend/.../runner/DevPermDemoModulesSeeder.java`）在 `dfbs.dev.seed-perm-demo-modules=true` 且非 prod 时写入：`platform_application`（平台应用）、`platform_application.orders`（订单，父节点为前者）；子模块动作 VIEW/CREATE/EDIT。
- **菜单与权限键**：`BasicLayout.tsx` 中用于门控的 permission key：`platform_application.orgs:VIEW`、`platform_application.applications:VIEW`（平台&网卡管理）、`work_order:VIEW`（售后服务 → 工单管理）。业务菜单名：报价单、物流管理、售后服务/工单管理、平台&网卡管理、主数据等；与 perm 模块的对应关系除上述门控外未在代码中集中枚举，work_order 映射见 `docs/perm-work-order-mapping-v1.md`。
- **结论**：模块列表以 DB `perm_module` 为准；默认库可为空；可选 dev 种子仅含 platform_application 及子节点；业务侧可见“工单”“平台”等与 `work_order`、`platform_application.*` 权限键对应。

**Item 2 — 账号管理 UI（Account Management）**

- **入口**：系统 → **账号与权限**（`/admin/account-permissions`），`BasicLayout.tsx` 中 `ACCOUNT_PERMISSIONS_MENU`，门控：`useIsAdminOrSuperAdmin()`。
- **页面**：`frontend/.../AccountPermissions/index.tsx`；顶部「默认密码」区块（`DefaultPasswordSection.tsx`）；Tabs：**账号**（`AccountsTab.tsx`）、**角色模板**（`RoleTemplatesTab.tsx`）、**权限管理**（`PermissionTreeTab.tsx`，仅 allowlist 可见）。
- **账号 Tab 行为**：搜索账号（可接 account-list 或 users 接口）、账号列表/表格，点击行打开**账号详情抽屉**；抽屉内：创建账号（绑定人员）、启用/停用、重置密码、**分配角色模板**（下拉）、**权限覆盖**（差异视图：继承自模板、追加权限、移除权限、最终生效；添加/移除通过选择器；高级编辑折叠）；保存/还原使用 `PUT .../accounts/{userId}/override`。角色分配与权限设置通过“选角色模板 + 覆盖（追加/移除）”完成，无单独“角色分配”页。
- **接口**：`acctPermService.ts` 调用 `/v1/admin/account-permissions/*`（people, accounts, users, account-list, override, roles, default-password 等）。

**Item 3 — 权限树结构与模块/动作定义**

- **key 格式**：`<moduleKey>:<actionKey>`，见 `PermissionTreeDto.KEY_FORMAT`（`backend/.../PermissionTreeDto.java`）、`V0071__perm_definition_v1.sql` 注释。
- **动作集**：`perm_action` 表种子（V0071）：VIEW/查看, CREATE/创建, EDIT/编辑, SUBMIT/提交, APPROVE/审批, REJECT/拒绝, ASSIGN/分配, CLOSE/关闭, DELETE/删除, EXPORT/导出；后端 `PermPermissionTreeService` 有同名单 fallback。
- **树结构**：`perm_module`（id, module_key, label, parent_id, enabled）+ `perm_module_action`（module_id, action_key）；树由 `PermPermissionTreeService.loadModuleTree()` 递归构建，每个节点含 key/label/actions/children 及 id/parentId/enabled。
- **接口**：`GET /api/v1/admin/perm/permission-tree` 返回 keyFormat、actions（ActionItem[]）、modules（树）；模块管理：`POST/PUT/DELETE /api/v1/admin/perm/modules`、`PUT .../modules/{id}/actions`（allowlist）。
- **校验**：`PermRoleService.validatePermissionKey` 要求 `moduleKey:actionKey` 且模块在 `perm_module` 存在、动作在 `perm_action` 或该模块的 `perm_module_action` 中存在。

---

## Not found items

- 无集中“业务模块清单”配置文件（如 Logistics Management、Quote 等与 perm module_key 的一一映射表）；菜单与门控分散在 `BasicLayout.tsx` 与各页，work_order 与 platform_application 的映射在代码与 `docs/perm-work-order-mapping-v1.md` 中可查。

---

## Full-suite build

未执行。可后续在 `backend/dfbs-app` 运行 `.\mvnw.cmd -q -DskipTests compile`，在 `frontend/dfbs-ui` 运行 `npm run build` 验证。

---

## Blocker question

无。
