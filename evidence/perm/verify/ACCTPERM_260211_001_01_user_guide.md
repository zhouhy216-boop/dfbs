# ACCTPERM Step-01 — 账号与权限入口 证据与用户验证指南

**Parent:** ACCTPERM-260211-001-01  
**Audience:** CEO / 初学者  
**Purpose:** 说明 Step-01 交付内容：新管理入口 **系统 → 账号与权限**（账号 / 角色模板 / 权限树 三个 Tab）；页面与接口的可见性及服务端门控规则；如何仅通过 UI 做验证。

---

## 一、新入口与 Tab

- **菜单位置：** 侧栏 **系统** 下新增 **「账号与权限」**，点击进入后路径为 `/admin/account-permissions`。
- **三个 Tab（CN）：**
  - **账号**：搜索/选择账号，查看与编辑该账号的**角色模板 + 添加/移除覆盖**，保存/还原；无权限树时仍可改模板与查看有效权限预览。
  - **角色模板**：角色列表的增删改、启用/停用、权限勾选（草稿 + 保存/还原）；无权限树时仅可改显示名称与启用状态。
  - **权限树**：只读展示当前权限树；新建模块、可编辑模块的编辑/删除/设置动作（仅对本页创建的模块可编辑）；所有变更通过接口立即生效。

---

## 二、前置条件（账号身份）

- **Admin / Super-admin（页面与账号/角色模板接口）：**
  - 用户 `authorities` 中含 **ROLE_ADMIN** 或 **ROLE_SUPER_ADMIN**（后端）；前端登录后 `userInfo.roles` 为 **ADMIN** 或 **SUPER_ADMIN**（AuthController 会去掉 ROLE_ 前缀）。
  - 仅上述账号能看到「账号与权限」菜单并访问该页；非 admin/super-admin 直接访问 `/admin/account-permissions` 会先「加载中...」再重定向到 `/dashboard`。
- **Allowlist（权限树 Tab + 权限树/模块接口）：**
  - 用户 ID 在配置 **dfbs.perm.superAdminAllowlist** 中（逗号分隔）。
  - 仅 allowlist 用户能看到「权限树」Tab；权限树与模块管理接口（`GET /api/v1/admin/perm/permission-tree`、`POST/PUT/DELETE /api/v1/admin/perm/modules`、`PUT .../modules/{id}/actions`）仅对 allowlist 开放，非 allowlist 请求返回 403。

---

## 三、可见性与服务端门控

| 范围 | 门控规则 |
|------|----------|
| 整页「账号与权限」 | 前端：AdminOrSuperAdminGuard（ROLE_ADMIN 或 ROLE_SUPER_ADMIN）；后端：`/api/v1/admin/account-permissions/*` 使用 AdminOrSuperAdminGuard，非 admin/super-admin 返回 403 PERM_FORBIDDEN。 |
| 账号 Tab、角色模板 Tab | 与整页相同；接口为 account-permissions 下的 users/accounts/override、roles/template 等，均不依赖 allowlist。 |
| 权限树 Tab | 前端：仅当 useIsPermSuperAdmin().allowed === true 时展示该 Tab；后端：权限树与模块接口为 allowlist-only，非 allowlist 返回 403。 |

---

## 四、UI 验证清单（≤12 条）

1. **菜单仅对 admin/super-admin 可见**：使用**非** ADMIN/SUPER_ADMIN 账号登录后，侧栏「系统」下**不显示**「账号与权限」；直接访问 `/admin/account-permissions` 会先显示「加载中...」再**重定向到 /dashboard**。
2. **Admin/super-admin 可见整页与账号、角色模板 Tab**：使用 ADMIN 或 SUPER_ADMIN 账号登录，能进入「账号与权限」页，能看到「账号」「角色模板」两个 Tab，并能正常使用（搜索账号、保存/还原、角色 CRUD、模板保存/还原）。
3. **权限树 Tab 仅 allowlist 可见**：非 allowlist 的 admin 用户**不显示**「权限树」Tab；allowlist 用户会看到第三个 Tab「权限树」。
4. **账号 Tab — 搜索与选择**：在账号 Tab 输入关键词搜索，选择账号后能加载该账号的覆盖数据（模板、添加/移除、有效权限预览）。
5. **账号 Tab — 保存与还原**：修改模板或添加/移除后点击「保存」会写入后端（PUT account-permissions/accounts/{userId}/override）；「还原」恢复为上次加载状态。
6. **账号 Tab — 无权限树时的降级**：非 allowlist 用户使用账号 Tab 时，若无法加载权限树，会显示「无权限查看权限树（需超级管理员）」；仍可切换角色模板、查看有效权限预览并保存。
7. **角色模板 Tab — 列表与 CRUD**：可新建角色、编辑（显示名称/启用）、删除；可选「显示全部（含停用）」查看停用角色。
8. **角色模板 Tab — 保存与还原**：选中角色后修改显示名称、启用状态或权限勾选，点击「保存」后生效；「还原」恢复为上次加载状态。
9. **角色模板 Tab — 无权限树时的降级**：非 allowlist 用户仅可修改显示名称与启用状态，不会误清空权限（保存时带当前权限键）。
10. **权限树 Tab — 只读树与模块操作**：allowlist 用户可查看当前权限树；可新建模块（key、显示名称、父节点）；本页创建的模块会在「可编辑模块」中列出，可编辑、删除、设置动作；删除有子节点的模块时提示「该模块下有子模块，请先删除子模块」。
11. **403 时的表现**：非 allowlist 调用权限树或模块接口时返回 403，权限树 Tab 内会显示「无权限」；非 admin 调用 account-permissions 接口时返回 403 PERM_FORBIDDEN。
12. **原有入口仍可用**：`/admin/roles-permissions`（角色与权限）仍对 allowlist 用户开放，行为与 Step-01 之前一致；平台/工单等 RBAC 鉴权逻辑未改。

---

## 五、回归说明

- **/admin/roles-permissions**：保留且仅受 allowlist 控制，与 Step-01 新增入口互不影响，向后兼容。
- **现有 RBAC**：平台管理、工单、effective-keys、角色模板与账号覆盖的「移除优先」等逻辑均未改动；Step-01 仅新增入口与 account-permissions 命名空间，不改变既有 perm 行为。

---

## 六、常见注意点

- **前端构建报错**：若 `npm run build` 因**其他文件**的 TypeScript 报错失败，与 Step-01 的「账号与权限」入口验证**无冲突**；可在本地用 admin/allowlist 账号登录后按上述清单逐条验证。
- **权限树 Tab 不出现**：确认当前用户 ID 在 `dfbs.perm.superAdminAllowlist` 中（且前端已请求 `/api/v1/admin/perm/super-admin/me` 得到 allowed: true）。

---

## 七、CEO 一句话验证要点

1. **谁能看到「账号与权限」**：仅 ADMIN 或 SUPER_ADMIN 能看到菜单并打开页面；其他人访问会跳回首页。
2. **谁能看到「权限树」Tab**：仅配置在 allowlist 中的用户能看到并操作权限树；其他管理员只能使用「账号」与「角色模板」两个 Tab。
3. **旧入口还在**：原来的「角色与权限」页面仍在，行为不变；新入口是补充，不是替换。
