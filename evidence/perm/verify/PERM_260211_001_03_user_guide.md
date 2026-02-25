# Step-03 角色模板 — 证据与用户验证指南

**Parent:** PERM-260211-001-03  
**Audience:** CEO / 初学者  
**Purpose:** Step-03 交付说明：角色模板的创建/编辑/启用停用、原子保存（“Save to apply”）、显示全部（含停用）、默认模板种子及非白名单行为。

---

## 一、Step-03 交付要点

- **角色模板**：左侧为模板列表，支持新建、编辑（名称）、删除；列表中展示**启用状态**（已启用/已停用）。
- **右侧编辑**：选中一个模板后，可修改**显示名称**、**启用**开关、**权限勾选**；所有修改均为**草稿**，仅当点击**「保存」**后一次性提交（原子保存：label + enabled + permissionKeys）。
- **「还原」**：放弃当前草稿，恢复为上次加载时的状态（名称、启用、权限一致）。
- **显示全部（含停用）**：勾选后列表重新请求并展示包括已停用在内的全部模板；取消勾选则只显示已启用的模板。

---

## 二、启动后端（PowerShell 可复制命令）

在 **Windows PowerShell** 中，从 **`backend/dfbs-app`** 执行。可按需组合使用。

### 1. 默认角色模板种子（推荐：保证至少有一个模板可操作）

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedPermDefaultRoleTemplates=true"
```

- 开关：`dfbs.dev.seedPermDefaultRoleTemplates=true`（默认 `false`）；profile 含 `prod` 时不执行。
- 会写入：`template_viewer`（只读模板，启用）、`template_editor`（编辑模板，停用），权限均为空。

### 2. Demo 模块树（可选：权限树有勾选项可测）

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedPermDemoModules=true"
```

- 开关：`dfbs.dev.seedPermDemoModules`；写入演示模块（如 平台应用/订单）及动作关联。

### 3. admin2 非白名单用户（可选：验证 403 / 菜单隐藏）

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedNonAllowlistUser=true"
```

- 开关：`dfbs.dev.seedNonAllowlistUser`；创建用户 admin2，不在 PERM 白名单内。

### 组合示例（默认模板 + demo 模块 + admin2）

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedPermDefaultRoleTemplates=true -Ddfbs.dev.seedPermDemoModules=true -Ddfbs.dev.seedNonAllowlistUser=true"
```

---

## 三、确认默认模板种子已执行（证据）

启动时若传入了 `dfbs.dev.seedPermDefaultRoleTemplates=true`，在**后端日志**中搜索：

- **DevPermDefaultRoleTemplatesSeeder**  
  - 出现 `flag=..., activeProfiles=...` 表示该 runner 已执行。  
  - 出现 `template_viewer=inserted|skipped (exists), template_editor=...` 表示插入或跳过情况。  
- 若出现 `skipping (prod profile active)`，表示因 prod 未插入。  
- 若**完全没有**上述日志，说明未开启该 bean，请检查 JVM 参数是否为 `-Ddfbs.dev.seedPermDefaultRoleTemplates=true` 且未激活 `prod` profile。

---

## 四、UI 验证清单（约 10 条，初学者友好）

1. **入口**：以白名单用户（如 admin）登录，侧栏有「角色与权限」，点击进入 `/admin/roles-permissions`。
2. **默认列表**：若已开启默认模板种子，列表至少显示「只读模板」（已启用）；勾选「显示全部（含停用）」后，列表刷新并出现「编辑模板」（已停用）。
3. **新建模板**：新建角色（输入 roleKey、显示名称），列表中出现新项。
4. **编辑名称**：在列表中对该角色点「编辑」，修改显示名称并保存，列表更新。
5. **选中后右侧**：选中一个模板后，右侧可改「显示名称」、切换「启用」、勾选/取消权限；此时为草稿，未点「保存」不落库。
6. **Save to apply**：点击「保存」后，名称、启用状态、权限一次性提交（原子保存）；刷新或重选再选回，三者与保存结果一致。
7. **还原**：修改名称/启用/权限后点「还原」，恢复为上次加载状态（与刷新后一致）。
8. **刷新一致性**：在角色与权限页刷新浏览器，列表与右侧内容正常，无异常跳转；菜单仍存在。
9. **登出/登录无权限泄漏**：登出后以 admin2 登录，侧栏无「角色与权限」，直接访问 `/admin/roles-permissions` 重定向到 `/dashboard`；PERM 接口返回 403。
10. **非白名单不变**：admin2 行为与 Step-02 一致，不展示 PERM 菜单与页面。

---

## 五、常见注意点

- **保存才生效**：右侧任意修改（名称、启用、权限）均需点「保存」后才会写入；仅改不保存则刷新后恢复为上次保存状态。
- **切换用户**：admin ↔ admin2 切换时需先登出再登录，前端才能根据 `super-admin/me` 正确显示或隐藏「角色与权限」。
- **PowerShell**：不要写 `--spring.profiles.active=dev` 在 `spring-boot:run` 后面（会报错）；用 `-Dspring-boot.run.jvmArguments=...` 或 `-Dspring-boot.run.profiles=dev`。

---

## 六、相关接口与配置（与实现一致）

| 用途 | 方法/路径 |
|------|------------|
| 是否显示 PERM 菜单/页 | `GET /api/v1/admin/perm/super-admin/me` |
| 权限树（只读） | `GET /api/v1/admin/perm/permission-tree` |
| 角色列表（可选 ?enabledOnly=true） | `GET /api/v1/admin/perm/roles` |
| 创建角色 | `POST /api/v1/admin/perm/roles` |
| 更新角色 | `PUT /api/v1/admin/perm/roles/{id}` |
| **原子保存模板（label+enabled+permissions）** | **`PUT /api/v1/admin/perm/roles/{id}/template`** |
| 删除角色 | `DELETE /api/v1/admin/perm/roles/{id}` |
| 角色已保存权限 | `GET /api/v1/admin/perm/roles/{id}/permissions` |

| 配置/开关 | 说明 |
|-----------|------|
| `dfbs.perm.superAdminAllowlist` | 逗号分隔 userId，在名单内才可访问 PERM |
| `dfbs.dev.seedPermDefaultRoleTemplates` | 是否写入默认角色模板（默认 false） |
| `dfbs.dev.seedPermDemoModules` | 是否写入演示模块树（默认 false） |
| `dfbs.dev.seedNonAllowlistUser` | 是否创建 admin2（默认 false） |

---

## 七、可选截图检查清单（无需嵌入）

- [ ] 默认列表有「只读模板」；勾选「显示全部（含停用）」后出现「编辑模板」。
- [ ] 选中模板后右侧可改名称、启用、权限；保存后刷新/重选后状态一致。
- [ ] 修改后点「还原」，恢复为上次加载状态。
- [ ] 刷新后页面与菜单稳定；登出后 admin2 无 PERM 菜单、直链重定向。
- [ ] 日志中可见 `DevPermDefaultRoleTemplatesSeeder` 的插入/跳过记录（若已开启默认模板种子）。
