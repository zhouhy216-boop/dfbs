# ACCTPERM Step-05 影响评估：账号覆盖 UX diff-only（仅事实）

**Request:** ACCTPERM-260211-001-05-IMP | **Scope:** 事实与回归清单，无代码修改。

**1) 当前前端**  
- 位置：`AccountsTab.tsx`。覆盖区含模块概览卡片（04.b）、PermissionMenuTree override（04.c）、下方「当前有效权限预览」；三态（默认/添加/移除）通过 Select + 菜单复选框 + 卡片快捷操作写 draftAddKeys/draftRemoveKeys。  
- 有效预览：`effectivePreview = (templateKeys ∪ draftAddKeys) \ draftRemoveKeys` 前端计算，Tag 列表（排序），无继承/覆盖/最终分块、无仅展示变更。  
- 草稿：savedOverride、draftRoleTemplateId、draftAddKeys、draftRemoveKeys；templateKeys 来自 `getRolePermissions(draftRoleTemplateId)`。Save=PUT(roleTemplateId, addKeys, removeKeys)，Reset=恢复 saved。diff-only 可加「仅展示变更」视图而不改保存/还原语义。

**2) 后端**  
- GET `.../accounts/{userId}/override` 返回：userId, roleTemplateId, roleTemplateKey, addKeys, removeKeys, effectiveKeys；**无** templateKeys。后端内部算 template 再 effective=template∪add∖remove。前端继承集 = getRolePermissions(draftRoleTemplateId)，与 add/remove 合并即 effective。

**3) diff-only 展示（事实约束）**  
- 可展示：继承(templateKeys)、覆盖(add/remove 列表)、最终有效(已有 effectivePreview)。默认可改为仅展示「添加」「移除」两列表，可选展开看全量 effective。避免：不改成全量表三态下拉；保留菜单树+卡片+三态，diff 仅作展示方式。

**4) 回归**  
- 移除优先、Save/Reset 语义不变；非 allowlist 仍无树/卡片、保存不清空权限；平台/工单 RBAC 不受影响。

**5) Build/test**  
未执行（仅文档）。验证命令：前端 `npm run build`、后端 `./mvnw -q compile`（可为既有 TS 错误阻断）。
