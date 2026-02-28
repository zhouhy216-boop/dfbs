# ACCTPERM Step-02-01 影响评估：账号列表页（仅事实）

**Request:** ACCTPERM-260211-002-01-IMP | **Scope:** 事实与回归清单，无代码修改。

**1) 前端**  
- 无独立账号列表表格页。账号能力在「系统 → 账号与权限」→ **账号** Tab（`AccountsTab.tsx`，路由 `/admin/account-permissions`）。  
- 当前为**搜索框 + Select 下拉**（`searchUsers(query)`）；选项显示 username、nickname、#id；选中后详情区：启用/停用、重置密码、模板、差异视图、高级编辑。无表格多列、无筛选/排序（仅关键词搜索）。  
- 可见字段：**id, username, nickname**（enabled 仅详情）；**无** Position、Department、Status、Role 列。  
- 角色/权限：选中账号后同 Tab 内管理（模板、追加/移除、保存 override）；GET/PUT `.../accounts/{userId}/override`、`getRolePermissions(roleId)`。操作：创建账号、启用/停用、重置密码、保存/还原 override；无列表行内操作。

**2) 后端**  
- GET `.../account-permissions/users?query=` 返回 `UserSummary[]`（id, username, nickname, enabled）；**query 空则返回空**；无分页；无 Position/Department/Role。GET `.../users/{id}` 同结构。  
- 角色/权限：GET|PUT `.../accounts/{userId}/override`（roleTemplateId, addKeys, removeKeys, effectiveKeys）；GET `.../roles`、`.../roles/{id}/permissions`。  
- `UserEntity` 有 org_person_id、enabled、authorities；`UserSummary` 无 org/role；无「账号+角色/部门/岗位」一体接口。

**3) 回归**  
- 账号列表改动不得破坏角色模板、权限管理、override 保存/还原、非 allowlist。角色/权限冲突或编辑错误需明确提示（现有 machineCode/message）。

**4) Build/test**  
未执行。验证：后端 `./mvnw -q compile`，前端 `npm run build`。
