# Step-02 角色与权限 — 证据与用户验证指南（仅 UI）

**Parent:** PERM-260211-001-02  
**Audience:** CEO / 初学者  
**Purpose:** 说明 Step-02 交付内容，以及如何在 dev/local 下**仅通过 UI** 完成验证，并避免常见坑。

---

## 一、Step-02 交付内容

- **动作（actions）**：默认动作集（查看/创建/编辑/提交/审批等），来自 `perm_action`，keyFormat 中的 `actionKey`。
- **模块（modules）**：树形模块节点（如 平台应用、订单），来自 `perm_module` + `perm_module_action`；默认可为空，需 dev 种子或后台维护后才有树。
- **角色（roles）**：角色列表与 CRUD，来自 `perm_role`。
- **绑定（bindings）**：角色 ↔ 权限（`moduleKey:actionKey`）的关联，来自 `perm_role_permission`；在 UI 上通过勾选权限树 + **保存** 生效。

权限 key 格式：**`<moduleKey>:<actionKey>`**（例如 `platform_application.orders:VIEW`）。详见 `docs/perm-permission-keys.md`。

---

## 二、如何启动后端以便验证

在 **Windows PowerShell** 中，从 **`backend/dfbs-app`** 执行。以下两条可单独或组合使用。

1. **admin2 非白名单用户（验证 403 / 菜单隐藏 / 直链重定向）**
   - 命令：  
     `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedNonAllowlistUser=true"`
   - 作用：创建用户 **admin2**（不在 `dfbs.perm.superAdminAllowlist` 内），用于验证无权限时的行为。
   - 开关：`dfbs.dev.seedNonAllowlistUser`（默认 `false`）；若激活 profile `prod` 则不会执行。

2. **Demo 模块树（让权限树有勾选项可测）**
   - 命令：  
     `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedPermDemoModules=true"`
   - 作用：写入演示模块（如 平台应用 / 订单）及 VIEW/CREATE/EDIT 等关联，权限树不再为空。
   - 开关：`dfbs.dev.seedPermDemoModules`（默认 `false`）；若激活 profile `prod` 则不会执行。

**同时启用两个种子（推荐验证时使用）：**  
`.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedNonAllowlistUser=true -Ddfbs.dev.seedPermDemoModules=true"`

- 入口页路径：**`/admin/roles-permissions`**（菜单名：「角色与权限」）。
- 可见性由 **`GET /api/v1/admin/perm/super-admin/me`** 控制；仅当返回 `allowed: true`（即当前用户 userId 在 `dfbs.perm.superAdminAllowlist` 中）时显示菜单并允许访问 PERM 相关接口。

---

## 三、UI 验证步骤（仅通过界面）

1. **角色 CRUD**
   - 左侧角色列表：**新建角色**（输入 roleKey、label）→ 列表中出现新角色。
   - **编辑**：改角色名称并保存。
   - **删除**：删除角色（若当前选中该角色，右侧会清空选中）。

2. **草稿 vs 保存 vs 重置（角色权限绑定）**
   - 选中左侧某一角色后，右侧为**该角色当前已保存的权限**；勾选/取消勾选仅改变**草稿**，未点「保存」不会落库。
   - **保存**：将当前勾选状态提交到后端（`PUT /api/v1/admin/perm/roles/{id}/permissions`），保存成功后草稿与“已保存”一致。
   - **重置**：放弃当前草稿，恢复为上次保存的状态。
   - 验证方式：勾选若干权限 → 保存 → 刷新页面或重选角色再选回，确认勾选仍存在；再改勾选 → 点重置，确认恢复为保存后的状态。

3. **非白名单用户（admin2）**
   - 用 **admin2** 登录（密码在 MVP 中可任意）。
   - 侧栏**不应**出现「角色与权限」；直接访问 **`/admin/roles-permissions`** 应**重定向到 `/dashboard`**。
   - 若直接调 **`GET /api/v1/admin/perm/permission-tree`**（或其它 PERM 管理接口），应返回 **403**，文案如「无权限」。

4. **刷新一致性**
   - 以 allowlist 内用户（如 admin）登录，打开「角色与权限」页，刷新浏览器：菜单仍存在，页面正常展示，无异常跳转。

---

## 四、常见注意点

- **保存才生效**：右侧权限勾选后必须点「保存」才会写入角色权限；只改勾选不点保存，刷新后会丢失未保存的修改。
- **登出/登录**：切换用户（如 admin ↔ admin2）时，需登出再登录，以便前端根据新的 `super-admin/me` 结果显示或隐藏「角色与权限」。
- **admin2 不要加入白名单**：不要将 admin2 的 userId 加入 `dfbs.perm.superAdminAllowlist`，否则无法验证 403/菜单隐藏/重定向。
- **PowerShell 传参**：不要写 `.\mvnw.cmd spring-boot:run --spring.profiles.active=dev`（Maven 会报错）；用 `-Dspring-boot.run.profiles=dev` 或 `-Dspring-boot.run.jvmArguments=...`。

---

## 五、相关接口与配置（与实现一致）

| 用途           | 方法/路径 |
|----------------|-----------|
| 是否显示 PERM 菜单/页 | `GET /api/v1/admin/perm/super-admin/me` |
| 权限树（只读）   | `GET /api/v1/admin/perm/permission-tree` |
| 角色列表       | `GET /api/v1/admin/perm/roles` |
| 创建角色       | `POST /api/v1/admin/perm/roles` |
| 更新角色       | `PUT /api/v1/admin/perm/roles/{id}` |
| 删除角色       | `DELETE /api/v1/admin/perm/roles/{id}` |
| 角色已保存权限 | `GET /api/v1/admin/perm/roles/{id}/permissions` |
| 保存角色权限   | `PUT /api/v1/admin/perm/roles/{id}/permissions` |

| 配置项 | 说明 |
|--------|------|
| `dfbs.perm.superAdminAllowlist` | 逗号分隔的 userId（字符串），在名单内才可访问 PERM 与菜单 |
| `dfbs.dev.seedNonAllowlistUser` | 是否创建 admin2（默认 false） |
| `dfbs.dev.seedPermDemoModules` | 是否写入演示模块树（默认 false） |

---

## 六、可选截图检查清单（无需嵌入文档）

- [ ] 以 allowlist 用户打开「角色与权限」：左侧角色列表 + 右侧权限树（若已开 demo 种子，可见平台应用/订单等）。
- [ ] 新建角色并编辑、删除，列表与选中状态正常。
- [ ] 选中角色 → 勾选/取消权限 → 保存 → 刷新/重选后勾选保持；再改勾选 → 重置 → 恢复为保存后状态。
- [ ] 以 admin2 登录：无「角色与权限」菜单；访问 `/admin/roles-permissions` 重定向到 `/dashboard`。
- [ ] 刷新后侧栏与「角色与权限」页稳定，无异常跳转。
