# Step-06 验证工具与审计 — 证据与用户验证指南

**Parent:** PERM-260211-001-06  
**Audience:** CEO / 初学者  
**Purpose:** 说明 Step-06 交付内容：**仅测试环境可用**的 Role-Vision 开关、测试账号套件（4 个账号）的生成/重置，以及 RBAC 变更的**粗粒度审计**；如何确认非测试环境返回 404、非白名单无法访问，以及登出/刷新无泄漏、后端鉴权不被 vision 绕过。

---

## 一、前置条件与环境安全

### 1.1 何时能看到验证工具

- **必须同时满足**：
  - 激活 **dev** profile（或非 prod），且
  - **`dfbs.perm.testUtilitiesEnabled=true`**（在 `application-dev.yml` 中已设为 `true`，主配置默认 `false`）。
- **prod 始终关闭**：只要激活了 **prod** profile，无论 flag 如何，Role-Vision 与测试账号相关接口均返回 **404**。
- **白名单**：当前用户必须在 `dfbs.perm.superAdminAllowlist` 内才能访问「角色与权限」页；测试工具入口也在该页，故**仅 super-admin 可见、可用**。

### 1.2 如何确认“非测试环境”行为

- 关闭 dev 或不设置 `dfbs.perm.testUtilitiesEnabled`，或使用 **prod** profile 启动后端。
- 用 Postman/curl 调用：
  - `GET /api/v1/admin/perm/test/vision`
  - `POST /api/v1/admin/perm/test/accounts/reset`
- 应得到 **404**（不是 403）。非白名单用户调用 `GET /api/v1/admin/perm/audit` 应得到 **403**。

---

## 二、UI 步骤

### 2.1 Role-Vision（测试）

- **出现位置**：以白名单用户登录 → 打开 **「角色与权限」**（`/admin/roles-permissions`）→ 若后端测试工具可用，页面顶部会出现一块 **「Role-Vision（测试）」** 区域（浅黄边框）。
- **操作**：
  - **视角** 下拉选 **关闭** 或 **按用户**。
  - 选 **按用户** 时，在搜索框输入用户名/昵称搜索，选择一名用户后点击 **应用**。
  - 应用后会出现提示：“当前以用户 #xxx 的权限查看菜单与按钮”；此时侧栏与平台管理/申请管理页的**菜单与按钮展示**按该用户权限决定（仅前端展示，**后端鉴权仍按当前登录用户**）。
- **恢复**：将视角选回 **关闭** 并点击 **应用**，或 **登出**（登出会清空 vision 状态）。

### 2.2 测试账号套件（4 个账号）

- **位置**：同上，在「Role-Vision（测试）」区域内，有按钮 **「生成/重置测试账号（4个）」**。
- **操作**：点击后，后端会创建或重置 4 个固定账号（`perm_t1_none`、`perm_t2_view`、`perm_t3_org_editor`、`perm_t4_app_approver`），并返回摘要。
- **预期结果**：页面显示 4 行，每行包含：**用户名**、**昵称**、**id（userId）**、**有效权限数**。可用这些账号登录或配合 Role-Vision「按用户」做 Step-05 验证（菜单隐藏、按钮隐藏、后端 403）。

---

## 三、审计步骤

### 3.1 会写审计的操作

- 以下操作成功执行后，会写入 **perm_audit_log** 一条记录（粗粒度，无 diff）：
  - **角色模板保存**：PUT `/api/v1/admin/perm/roles/{id}/template`
  - **账号 override 保存**：PUT `/api/v1/admin/perm/accounts/{userId}/override`
  - **模块**：POST/PUT/DELETE `/api/v1/admin/perm/modules` 及 PUT `.../modules/{id}/actions`
  - **测试套件重置**：POST `/api/v1/admin/perm/test/accounts/reset`
  - **Role-Vision 设置**：POST `/api/v1/admin/perm/test/vision`

### 3.2 如何查询审计

- **接口**（需白名单，同其他 PERM 管理接口）：
  - **GET** `/api/v1/admin/perm/audit?limit=10`
  - 可选参数：`actionType`、`targetType`、`targetId`（用于筛选），`limit` 默认 50，最大 200。
- **示例**：  
  `GET /api/v1/admin/perm/audit?limit=10`  
  返回 JSON 数组，按 **created_at 降序**（最新在前）。
- **字段含义**（每条记录）：
  - **actor_user_id / actor_username**：谁执行了操作
  - **action_type**：如 `ROLE_TEMPLATE_SAVE`、`ACCOUNT_OVERRIDE_SAVE`、`MODULE_CREATE`、`MODULE_UPDATE`、`MODULE_DELETE`、`MODULE_ACTIONS_SET`、`TEST_KIT_RESET`、`VISION_SET`
  - **target_type**：`ROLE` / `USER` / `MODULE` / `SYSTEM`
  - **target_id / target_key**：被操作对象 id 或 key（如 roleId、userId、moduleKey）
  - **created_at**：操作时间
  - **note**：简短说明（如 `enabled=true`, `permissionKeysCount=12`, `count=4`）

---

## 四、回归检查

- **登出/登录与刷新**：登出后 vision 状态清空；再登录或刷新，不应出现“用上一用户权限看到菜单/按钮”的泄漏；测试账号列表与审计查询仅对当前白名单用户可见。
- **Step-05 未被绕过**：开启 Role-Vision「按用户」查看某低权限用户时，**前端**菜单/按钮按该用户权限隐藏；但**后端**接口（如 POST 创建机构、POST 创建申请）仍按**当前登录用户**鉴权。用仅 VIEW 的账号登录后直接调创建接口，应得到 **403 PERM_FORBIDDEN**；vision 仅影响前端展示，不改变任何接口的鉴权结果。

---

## 五、常见注意点（Common pitfalls）

- **前端构建失败**：若 `npm run build` 因**其他文件**的 TypeScript 报错失败，与 Step-06 的 Role-Vision、测试账号、审计验证**无冲突**；可在本地用 dev 配置启动后端，用浏览器打开「角色与权限」+ 接口工具调 `/audit` 完成验证。
- **看不到 Role-Vision 区域**：确认后端以 **dev** profile 启动且 `dfbs.perm.testUtilitiesEnabled=true`（如使用 `application-dev.yml`）；且当前用户在白名单内。
- **审计里没有记录**：确认执行的是“会写审计”的操作（见 3.1），且操作**成功**（返回 2xx）；审计写入为 best-effort，一般不会影响主流程。

---

## 六、CEO 一句话验证要点

1. **测试环境**：在 dev + testUtilitiesEnabled 下，白名单用户可在「角色与权限」页使用 Role-Vision（关闭/按用户）和「生成/重置测试账号（4个）」；非测试环境或 prod 下相同接口应返回 **404**。
2. **审计**：保存角色模板、账号 override 或执行测试套件重置后，调用 **GET /api/v1/admin/perm/audit?limit=10** 应看到对应记录（谁、什么操作、目标、时间）。
3. **安全**：登出后 vision 清空、无权限泄漏；用 vision 查看低权限用户时，后端接口仍按登录用户鉴权，Step-05 的 403 行为不变。
