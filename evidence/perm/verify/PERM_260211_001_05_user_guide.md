# Step-05 平台应用强制示例 — 证据与用户验证指南

**Parent:** PERM-260211-001-05  
**Audience:** CEO / 初学者  
**Purpose:** 提供可**仅通过 UI + 一次接口绕过**验证的 Step-05 证据：前端按权限隐藏菜单/页面/按钮（仅隐藏，无“禁用+原因”）；后端对关键操作返回 403 + machineCode `PERM_FORBIDDEN`；有效权限来自「模板 ∪ 添加 - 移除」（移除优先）。

**映射文档（v1）：** `docs/perm-platform-application-mapping-v1.md`

---

## 一、前置条件

### 1.1 如何保证演示模块存在

- Step-05 管控的权限 key 属于 **平台管理**（`platform_application.orgs`）与 **申请管理**（`platform_application.applications`）。若权限树中尚无这些模块/动作，可：
  - 在「角色与权限」→ **角色模板** 中，通过模块管理维护 `platform_application.orgs`、`platform_application.applications` 及其动作（VIEW、CREATE、EDIT、DELETE、SUBMIT、APPROVE、REJECT、CLOSE）；或
  - 使用已包含上述模块的 dev 种子/环境。
- **白名单**：用于配置账号的当前用户需在 `dfbs.perm.superAdminAllowlist` 内，才能访问「角色与权限」页（`/admin/roles-permissions`）。

### 1.2 如何用 Step-04 配置测试账号

在「角色与权限」页切换到 **「按账号覆盖」**，搜索并选择账号后：

- **分配角色模板**：选择已启用且包含平台相关权限的模板（或先建模板并勾选 `platform_application.orgs:*` / `platform_application.applications:*` 所需动作）。
- **添加/移除**：对具体 key 选「默认 / 添加 / 移除」；**有效权限 = (模板权限 ∪ 添加) \\ 移除**（移除优先）。

建议准备三类测试账号（可同一用户不同环境，或三个不同用户）：

| 类型 | 目的 | 配置要点 |
|------|------|----------|
| **仅 VIEW** | 验证菜单/页面可见、操作按钮隐藏 | 模板或添加仅含 `platform_application.orgs:VIEW`、`platform_application.applications:VIEW`，无 CREATE/EDIT/DELETE/SUBMIT/APPROVE/REJECT/CLOSE。 |
| **无 VIEW** | 验证菜单隐藏、直链重定向 | 不分配含平台 VIEW 的模板，或对 VIEW 选「移除」。 |
| **VIEW + 某一关键动作** | 验证该动作可见且后端放行、其他关键动作隐藏且后端 403 | 例如仅添加 `platform_application.orgs:CREATE`，则「新建机构」可见且 POST 创建不 403；无 EDIT/DELETE 则编辑/删除隐藏且 PUT 应 403。 |

保存后，用对应用户登录前端进行下方清单验证。

---

## 二、UI 验证清单（≤12 条）

### A) 菜单隐藏与直链重定向（缺 VIEW）

1. **平台管理（orgs）**：无 `platform_application.orgs:VIEW` 时，侧栏「平台&网卡管理」下**不显示**「平台管理」；浏览器直接打开 `/platform/orgs` → **重定向到 `/dashboard`**（或等同安全页）。
2. **申请管理（applications）**：无 `platform_application.applications:VIEW` 时，侧栏**不显示**「申请管理」；直接打开 `/platform/applications` → **重定向到 `/dashboard`**。
3. 有对应 VIEW 时，侧栏显示该入口，且进入页面正常（不重定向）。

### B) 按钮/操作隐藏（缺对应动作权限）

4. **平台管理页**：无 CREATE 时**不显示**「新建机构」；无 EDIT 时不显示行内「编辑」、启用开关为只读展示；无 DELETE 时不显示「删除」。
5. **申请管理页**：无 SUBMIT/CLOSE 时不显示「营企处理」及弹窗内「提交至管理员」「关闭申请」；无 APPROVE/REJECT 时不显示「管理员审核」及弹窗内「通过」「驳回」。
6. 仅 VIEW 的账号进入页面后，列表/详情可见，但上述操作按钮均不出现。

### C) 后端防护（绕过 UI 触发关键操作 ⇒ 403）

7. 用**仅 VIEW**（或无对应关键权限）的账号，通过接口工具（如 Postman/curl）带该用户 token 与 **X-User-Id** 调用：
   - 平台机构：`POST /api/v1/platform-orgs` 或 `PUT /api/v1/platform-orgs/{id}`（body 含 status=DELETED 视为 DELETE）；
   - 平台申请：`POST /api/v1/platform-account-applications/create`、`PUT .../planner-submit`、`POST .../approve`、`reject`、`close`。
8. 上述请求应返回 **HTTP 403**，响应体为统一 JSON（如 ErrorResult），且 **machineCode 为 `PERM_FORBIDDEN`**。
9. 拥有对应关键权限的账号用同样方式调用，应返回 2xx（业务成功或业务校验失败，而非 403 无权限）。

### D) 刷新 / 登出 / 登录无泄漏

10. 有权限用户打开平台管理或申请管理页后**刷新**：菜单与按钮仍按当前权限显示，无短暂露出再消失。
11. **登出**后以无 VIEW 账号登录：侧栏无「平台管理」「申请管理」；直接访问其 URL 仍重定向到 `/dashboard`。
12. 切换回有权限账号登录：菜单与按钮恢复，无上一用户权限残留。

---

## 三、映射参考

- 路由/菜单、按钮、后端接口与权限 key 的对应关系见 **`docs/perm-platform-application-mapping-v1.md`**（Section A/B/C）。
- 关键 key 示例：
  - 平台管理：`platform_application.orgs:VIEW`、`:CREATE`、`:EDIT`、`:DELETE`
  - 申请管理：`platform_application.applications:VIEW`、`:CREATE`、`:SUBMIT`、`:APPROVE`、`:REJECT`、`:CLOSE`

---

## 四、常见注意点（Common pitfalls）

- **前端构建失败**：若 `npm run build` 因**其他文件**（如 OrgTree、WorkOrder、ImportCenter 等）的 TypeScript 报错失败，与 Step-05 的 UI 隐藏与后端 403 验证**无冲突**；可在本地仅启动前后端，用浏览器 + 接口工具完成上述清单，无需通过全量前端构建。
- **权限不生效**：确认在「按账号覆盖」中已**保存**；前端从 `GET /api/v1/perm/me/effective-keys` 拉取当前用户有效 key，与后端「模板 ∪ 添加 - 移除」一致。
- **403 但无 PERM_FORBIDDEN**：Step-05 无权限时统一为 403 + machineCode `PERM_FORBIDDEN`；若看到其他 403 或 code，可能是鉴权/白名单等其他逻辑，需区分。

---

## 五、CEO 一句话验证要点

1. **无权限时**：侧栏不显示平台管理/申请管理，直链打开会跳到首页；有 VIEW 无动作时，页面能看但「新建/编辑/删除/提交/通过/驳回/关闭」等按钮不显示。
2. **绕过界面**：用接口直接发创建/编辑/删除/提交/审批/关闭请求且不带对应权限 → 必须返回 **403** 且 body 里 **machineCode 为 PERM_FORBIDDEN**。
3. **换人/刷新**：登出换账号或刷新后，菜单与按钮只跟当前登录用户权限一致，无权限泄漏或错乱。
