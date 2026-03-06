# SHIPFLOW-260304-001-05 手动标记异常与异常记录可见 — 冒烟/回归检查清单

**Ticket:** SHIPFLOW-260304-001-05  
**Purpose:** Step-05 可被 CEO 快速复验：手动标记异常表单（无自动检测表述）、异常记录落库并在详情可见、可选设备与无效设备提示、权限控制一致。

---

## 前置条件

- **后端已部署 Step-05.b：**
  - 表 `shipment_exception_record` 存在；`POST /api/v1/shipments/{id}/exception` 接受 `ExceptionMarkRequest`（reason 必填；machineId/exceptionType/responsibility/evidenceUrl 可选），在翻状态为 EXCEPTION 的同时插入一条异常记录。
  - `GET /api/v1/shipments/{id}/exceptions`（可选 `machineId`）返回该发运的异常记录列表；需 VIEW 权限。
  - machineId 不属于该发运时返回 400，machineCode `SHIPMENT_MACHINE_NOT_FOUND`。
  - 标记异常需 `shipment.shipments:EXCEPTION`；workflow 按权限过滤 EXCEPTION 动作。
- **前端已部署 Step-05.c：** 详情 Drawer 中「标记异常」改为表单弹窗（原因必填、类型/责任/凭证链接/关联设备可选）；提交后刷新详情与异常记录；详情内展示「异常记录」列表；无 EXCEPTION 权限不展示「标记异常」按钮。
- 至少两个账号：一个无 `shipment.shipments:EXCEPTION`，一个具备 VIEW + EXCEPTION。
- 至少一条可标记异常的发运单（CREATED/PENDING_SHIP/SHIPPED，非 COMPLETED/CANCELLED）；建议有一条已关联机器的发运单（用于设备维度验证）。

---

## 冒烟步骤（8–10）

| # | 步骤 | 预期结果 |
|---|------|----------|
| 1 | 用有 EXCEPTION 权限的账号打开一条可标记异常的发运单详情 | 可执行操作中含「标记异常」；文案/表单无“自动检测”等表述。 |
| 2 | 点击「标记异常」 | 弹出「标记异常（人工填写）」表单：异常原因*（必填，提示人工填写）、异常类型、责任方、凭证链接（可粘贴链接）、关联设备（仅当该发运有机器列表时显示）。 |
| 3 | 仅填写异常原因后提交 | 请求 `POST .../exception` 成功；状态变为异常；弹窗关闭；详情刷新；下方「异常记录」区域出现一条新记录（时间、说明等）。 |
| 4 | 再次打开该发运详情（或刷新） | 「异常记录」列表中仍显示刚提交的记录；时间、说明、可选类型/责任/凭证链接正确展示；凭证链接可点击。 |
| 5 | 对有关联机器的发运单，在「标记异常」表单中选择「关联设备」后提交 | 记录中带 machineId；异常记录列表中该条显示对应设备（机器号或 #id）。 |
| 6 | 故意提交无效 machineId（如用接口工具传不属于该发运的 machineId） | 后端返回 **400**；前端提示「所选设备不属于该发运单」（或等价中文）。 |
| 7 | 使用**无** `shipment.shipments:EXCEPTION` 的账号打开同一条发运详情 | 「可执行操作」中**不**展示「标记异常」按钮。 |
| 8 | 用无 EXCEPTION 权限的账号直接调用 `POST /api/v1/shipments/{id}/exception` | 响应 **403**，body 含 PERM_FORBIDDEN。 |
| 9 | 用无 VIEW 权限的账号调用 `GET .../exceptions` | 响应 **403**。 |
| 10 | 对 CREATED/PENDING_SHIP 发运执行接单、发运、标记异常等 | 与 Step-01～04 无退步；异常记录仅人工触发，无自动检测相关文案。 |

---

## 预期结果汇总

- **手动表单**：标记异常为人工操作；表单标题/占位为「人工填写」「可粘贴链接」等，无自动检测表述。  
- **记录可见**：每次标记异常后插入一条记录；详情 Drawer 内「异常记录」列表展示时间、设备（如有）、类型/责任、说明、凭证链接；打开/刷新详情时列表与 GET exceptions 一致。  
- **可选 machineId**：有机器列表时表单展示关联设备下拉；提交后记录带 machineId；无效 machineId 时 400 + 前端提示「所选设备不属于该发运单」。  
- **权限**：无 EXCEPTION 不展示「标记异常」；无 VIEW 不能 GET exceptions；无 EXCEPTION 时 POST exception 返回 403。

---

## 已知限制（回归观察项，供 CEO）

1. **前端全量构建**：项目内其他文件存在既有 TypeScript 错误，`npm run build` 可能失败；本页在 `npm run dev` 下可验证，`src/pages/Shipment/index.tsx` 无 TS 报错。  
2. **凭证**：v1 仅支持凭证链接（evidenceUrl），无文件上传。  
3. **设备下拉**：仅当该发运已有关联机器（GET machines 有数据）时显示「关联设备」；无机器时仍可提交发运级异常。
