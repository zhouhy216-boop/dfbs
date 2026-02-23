# 权限 key 约定（PERM v1）

本文档描述角色与权限模块 v1 的权限 key 格式与默认动作集，供扩展时参考。

## keyFormat

权限 key 格式：**`<moduleKey>:<actionKey>`**

- `moduleKey`：模块标识，如 `platform_application`（平台应用）。
- `actionKey`：动作标识，如 `VIEW`、`CREATE`。
- 示例：`platform_application:VIEW` 表示「平台应用」模块的「查看」权限。

## v1 默认动作集

| actionKey | 中文名 |
|-----------|--------|
| VIEW      | 查看   |
| CREATE    | 创建   |
| EDIT      | 编辑   |
| SUBMIT    | 提交   |
| APPROVE   | 审批   |
| REJECT    | 拒绝   |
| ASSIGN    | 分配   |
| CLOSE     | 关闭   |
| DELETE    | 删除   |
| EXPORT    | 导出   |

以上为 v1 约定的通用动作集合，在接口「默认动作（v1）」中展示；各模块可引用其中全部或子集。

## 如何扩展

- **新增模块**：在服务端维护的模块列表中增加新的 `moduleKey` 与 `label`，并为其配置 `actions`（可复用默认 actionKey 或后续扩展新 actionKey）。无需改 key 格式。
- **新增动作**：若将来需要新的通用动作，在默认动作集中增加新的 `actionKey` 与中文名，并在需要该动作的模块的 `actions` 中加入即可。
- 本约定不承诺对已有 key 的兼容性策略（如重命名、废弃）的细化；扩展时尽量只做增量添加。

## 相关接口

- `GET /api/v1/admin/perm/permission-tree`：返回 keyFormat、默认动作列表、模块树（只读，需 PERM 超级管理员 allowlist）。
