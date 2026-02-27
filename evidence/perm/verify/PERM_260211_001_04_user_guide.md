# Step-04 按账号覆盖 — 证据与用户验证指南

**Parent:** PERM-260211-001-04  
**Audience:** CEO / 初学者  
**Purpose:** Step-04 交付说明：为账号分配角色模板、配置添加/移除覆盖；合并规则「移除优先」；仅点击「保存」后生效；支持「还原」恢复上次保存状态。

---

## 一、前置条件（复制即用）

- **白名单**：当前用户 userId 在 `dfbs.perm.superAdminAllowlist` 内，可访问「角色与权限」页（`/admin/roles-permissions`）。
- **Demo 模块（可选）**：若需在「按账号覆盖」中看到权限树勾选（默认/添加/移除），可开启 demo 模块种子：
  ```powershell
  .\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedPermDemoModules=true"
  ```
- **admin2（回归用）**：验证非白名单仍无 PERM 访问：
  ```powershell
  .\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedNonAllowlistUser=true"
  ```
- **默认模板（可选）**：若需至少一个可分配模板，可开启默认角色模板种子（见 Step-03 用户指南）。

---

## 二、Step-04 交付要点

- **按账号覆盖**：在「角色与权限」页的 **「按账号覆盖」** 标签下，先**搜索并选择账号**，再为该账号：
  - **分配角色模板**：从下拉选择（仅已启用模板）；不选则无模板基准。
  - **添加/移除覆盖**：对每个权限 key 选择 **默认**（继承模板）、**添加**、**移除**；**移除优先**：若某 key 在移除中，则有效权限中不包含该 key，即使其在模板或添加中。
- **有效权限**：`有效 = (模板权限 ∪ 添加) \ 移除`，与接口 `GET /api/v1/admin/perm/accounts/{userId}/override` 返回的 `effectiveKeys` 一致。
- **保存才生效**：所有修改为草稿；点击 **「保存」** 后调用 `PUT .../accounts/{userId}/override` 一次性写入；**「还原」** 将草稿恢复为上次加载（上次保存）状态。

---

## 三、UI 验证清单（≤12 条）

1. **入口**：白名单用户登录，打开「角色与权限」页，切换到 **「按账号覆盖」** 标签。
2. **账号搜索与选择**：在「搜索账号」输入框输入用户名或昵称关键词，调用 `GET /api/v1/admin/perm/users?query=...`，下拉展示匹配结果（id、username、nickname）；选择一名用户后，加载该账号的 override（`GET /api/v1/admin/perm/accounts/{userId}/override`）。
3. **分配模板**：在「分配角色模板」下拉中选择一个**已启用**的模板；若后端仅允许已启用模板，选择已停用模板会保存时报错（PERM_ROLE_DISABLED），文案如「只能分配已启用的角色模板」。
4. **添加/移除三态**：每个权限 key 可选 **默认** / **添加** / **移除**；「移除」在 UI 上应明显（如红色/错误态），表示移除优先。
5. **移除优先规则**：某 key 设为「移除」后，无论其是否在模板或「添加」中，**当前有效权限预览**中均不包含该 key。
6. **有效权限预览**：页面下方「当前有效权限预览（模板 + 添加 - 移除）」与上述规则一致；预览随草稿实时更新。
7. **Save to apply**：修改模板或添加/移除后**不点保存**，切换账号或刷新页面，再选回该账号，应恢复为上次保存的状态（未保存的修改丢失）。
8. **保存持久化**：点击「保存」后，再切换账号或刷新后选回该账号，应看到刚保存的模板与添加/移除一致。
9. **还原**：修改后点击「还原」，草稿恢复为上次加载（上次保存）状态；与不保存就刷新效果一致。
10. **回归：角色模板 Tab**：「角色模板」标签行为与 Step-03 一致（列表、显示全部含停用、选中模板编辑、保存/还原、权限树勾选）。
11. **回归：admin2**：登出后以 admin2 登录，无「角色与权限」菜单；直接访问 `/admin/roles-permissions` 重定向到 `/dashboard`；无权限泄漏。
12. **回归：刷新与登出**：在「按账号覆盖」或「角色模板」下刷新，页面与菜单正常；登出再登录后，PERM 可见性仅由新用户是否在白名单决定。

---

## 四、常见问题（Common mistakes）

- **前端构建失败（现有 TS 报错）**：若 `npm run build` 因其他文件（如 OrgTree、BasicLayout、WorkOrder 等）的 TypeScript 错误失败，与 Step-04 功能验证无关；可在本地仅跑角色与权限相关页面做 UI 验证。
- **权限树为空**：若「按账号覆盖」下看不到权限树或只有「暂无模块」，请开启 **demo 模块** 种子（见前置条件），以便看到模块与默认/添加/移除选择。
- **账号搜索无结果**：确认后端已启动且 `GET /api/v1/admin/perm/users?query=...` 可访问（白名单）；查询会匹配 username、nickname（不区分大小写）。

---

## 五、相关接口（与实现一致）

| 用途 | 方法/路径 |
|------|------------|
| 用户搜索（账号选择） | `GET /api/v1/admin/perm/users?query=...` |
| 读取账号 override | `GET /api/v1/admin/perm/accounts/{userId}/override` |
| 保存账号 override（原子） | `PUT /api/v1/admin/perm/accounts/{userId}/override` |

响应 `effectiveKeys` 由服务端按「模板 ∪ 添加 - 移除」计算；请求体为 `{ roleTemplateId, addKeys[], removeKeys[] }`（replace-style）。
