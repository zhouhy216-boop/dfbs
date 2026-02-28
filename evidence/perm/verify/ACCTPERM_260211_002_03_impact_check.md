# ACCTPERM-260211-002-03 影响评估：默认密码管理（仅事实）

**Request:** ACCTPERM-260211-002-03-IMP  
**Scope:** 实施前事实与回归清单，无代码修改。

---

## 1) 当前默认密码使用点

**Backend**

- **定义/读取**：`AuthProperties`（`@ConfigurationProperties(prefix = "dfbs.auth")`）在 `config/AuthProperties.java` 中定义 `defaultPassword`，默认 `"changeme"`；`getDefaultPassword()` 读值。绑定在 `JpaAuditingConfig` 的 `@EnableConfigurationProperties(AuthProperties.class)`；`application.yml` 中 `dfbs.auth.defaultPassword: changeme`。
- **账号创建（绑定人员）**：`AdminAccountService.createAccount`（`application/account/AdminAccountService.java`）第 67 行：`u.setPasswordHash(passwordEncoder.encode(authProperties.getDefaultPassword()))`；新建账号的 password_hash 一律用当前 defaultPassword 编码写入。
- **重置密码**：`AdminAccountService.resetPassword`（同上）第 96 行：`String toEncode = (newPassword != null && !newPassword.isBlank()) ? newPassword : authProperties.getDefaultPassword();`；未传或空 `newPassword` 时使用 defaultPassword。
- **登录（legacy password_hash 为 NULL）**：`AuthController.login`（`interfaces/auth/AuthController.java`）第 46–50 行：若 `hash == null || hash.isBlank()`，则用 `rawPassword.equals(authProperties.getDefaultPassword())` 校验；否则 BCrypt 校验。

---

## 2) 存储与审计（事实）

- **当前**：无“系统设置/配置”表存默认密码；仅 `application.yml` + `AuthProperties`，重启/部署后生效。
- **若需管理员可改且重启后保留**：需新增持久化（例如独立 key-value 表或 `app_setting` 之类），或约定“改配置 + 重启”由运维完成；目前代码中无现成 auth 用 settings 表可复用。
- **审计**：`PermAuditService`（`application/perm/PermAuditService.java`）已有 `ACTION_ACCOUNT_PASSWORD_RESET`；无 `DEFAULT_PASSWORD_CHANGED`。可新增 action 类型并在“管理员修改默认密码”处调用 `auditService.log(...)`；现有 `log(actionType, targetType, targetId, targetKey, note)` 可复用，无需改表。

---

## 3) 安全与门控（事实）

- **重置密码接口**：`AccountPermissionsController.resetPassword` 使用 `adminGuard.requireAdminOrSuperAdmin()`（`AdminOrSuperAdminGuard`），即 ROLE_ADMIN 或 ROLE_SUPER_ADMIN。
- **惯例**：敏感管理接口用 403（`PermForbiddenException` / `ResponseStatusException(FORBIDDEN)`）；404 用于“功能不可用”（如 `PermTestUtilitiesGuard` 在非测试环境对测试工具返回 404）。默认密码管理建议与现有 account-permissions 一致：同一 guard，403 拒绝未授权。

---

## 4) 回归清单

- 修改默认密码后，**新创建账号**必须使用新的默认密码（当前仅 `AdminAccountService.createAccount` 一处写 password_hash，依赖 `authProperties.getDefaultPassword()`，需保证该来源为“当前生效的默认密码”）。
- **重置密码**未填新密码时必须使用新的默认密码（当前 `AdminAccountService.resetPassword` 已用 `authProperties.getDefaultPassword()`，同上）。
- **Legacy 登录**（password_hash 为 NULL）：必须与上述同一默认密码源一致，避免部分请求用旧值、部分用新值。
- 不改变现有 RBAC（模板/覆盖/校验）、不扩大权限暴露；审计仅新增 DEFAULT_PASSWORD_CHANGED，不破坏现有审计写入。

---

## 5) Build status

未执行。可后续用 `.\mvnw.cmd -q -DskipTests compile` 在 `backend/dfbs-app` 下验证。
