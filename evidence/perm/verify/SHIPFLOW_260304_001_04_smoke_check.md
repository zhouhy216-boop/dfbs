# SHIPFLOW-260304-001-04 签收确认与关闭 — 冒烟/回归检查清单

**Ticket:** SHIPFLOW-260304-001-04（Step-04 签收 + 关闭）  
**Purpose:** Step-04 可被 CEO 快速复验：签收确认（v1 不强制回单）、关闭仅在被签收后可用且需 CLOSE 权限、步骤文案「待关闭/已关闭」、workflow 与权限一致。

---

## 前置条件

- **后端已部署 Step-04.b：**
  - `POST /api/v1/shipments/{id}/complete`：v1 下 receiptUrl 为可选；无回单也可完成签收。
  - 表 `shipment` 有 `closed_at` 字段；`POST /api/v1/shipments/{id}/close?operatorId=...` 仅在 status=COMPLETED 且 closedAt 为空时设置 closedAt。
  - `GET .../workflow` 的 stepLabelCn：COMPLETED 且 closedAt 为空时为「已签收（待关闭）」，closedAt 有值时为「已关闭」；COMPLETED 且未关闭时 actions 含 CLOSE「关闭」。
  - 权限键 `shipment.shipments:CLOSE`；POST close 需该权限；workflow 按 effectiveKeys 过滤，无 CLOSE 不返回 CLOSE 动作。
- **前端已部署 Step-04.c：** 发运详情 Drawer 识别 actionCode CLOSE，调用 POST close，成功后刷新详情与 workflow；前端常量含 PERM_CLOSE，按钮按 permKeyForAction 过滤。
- 至少两个账号：一个无 `shipment.shipments:CLOSE`，一个具备 VIEW + COMPLETE + CLOSE（及所需其他操作权限）。
- 至少一条 SHIPPED 状态发运单（用于走完签收→关闭流程）。

---

## 冒烟步骤（8–10）

| # | 步骤 | 预期结果 |
|---|------|----------|
| 1 | 对一条 **SHIPPED** 发运单打开详情 Drawer | 当前步骤为「运输中」；可执行操作含「签收确认」（对应 COMPLETE）。 |
| 2 | 在**未上传回单**（无 receiptUrl）的情况下点击「签收确认」并确认 | 请求 `POST .../complete` 成功；状态变为 COMPLETED；当前步骤文案变为「已签收（待关闭）」；可执行操作出现「关闭」按钮。 |
| 3 | 有回单时执行「签收确认」 | 同样可完成；后端若校验回单则仅在有 receiptUrl 时校验，无回单不阻塞。 |
| 4 | 在步骤 2 之后（COMPLETED、未关闭）点击「关闭」并确认 | 请求 `POST .../close` 成功；当前步骤文案变为「已关闭」；「关闭」按钮消失；详情与可执行操作已刷新。 |
| 5 | 对一条 **SHIPPED**（未签收）发运单，直接调用 `POST /api/v1/shipments/{id}/close?operatorId=1` | 后端返回 **400** 或等价错误（仅已签收可关闭）；不设置 closedAt。 |
| 6 | 使用**无** `shipment.shipments:CLOSE` 的账号打开已签收未关闭的发运单详情 | 「可执行操作」中**不**展示「关闭」按钮（前端按权限过滤）。 |
| 7 | 使用无 CLOSE 权限的账号直接调用 `POST .../close` | 响应 **403**，body 含 PERM_FORBIDDEN（或项目约定 403 结构）。 |
| 8 | 调用 `GET /api/v1/shipments/{id}/workflow`，账号无 CLOSE 权限 | 返回的 `actions` **不**包含 actionCode 为 CLOSE 的项。 |
| 9 | 已关闭的发运单再次打开详情 | 当前步骤为「已关闭」；无「关闭」按钮；其他只读信息正常展示。 |
| 10 | 对 CREATED/PENDING_SHIP 发运单执行接单、备货、发运、签收、关闭全流程 | 与 Step-01/02/03 无退步；签收可不带回单完成；关闭后步骤与按钮符合上述预期。 |

---

## 预期结果汇总

- **签收确认（COMPLETE）**：v1 下不强制回单；无 receiptUrl 也可完成；完成后 stepLabelCn 为「已签收（待关闭）」，出现「关闭」操作。  
- **关闭（CLOSE）**：仅在 status=COMPLETED 且 closedAt 为空时可执行；需 `shipment.shipments:CLOSE` 权限；执行后 stepLabelCn 为「已关闭」，不再展示「关闭」按钮。  
- **stepLabelCn**：COMPLETED 未关闭 →「已签收（待关闭）」；COMPLETED 已关闭 →「已关闭」；由后端 workflow 返回，前端直接展示。  
- **workflow 与权限一致**：GET workflow 按 effectiveKeys 过滤 actions；无 CLOSE 权限时无 CLOSE 动作；前端按钮与后端返回一致，避免“有按钮但 403”。

---

## 已知限制（回归观察项，供 CEO）

1. **前端全量构建**：项目内其他文件存在既有 TypeScript 错误，`npm run build` 可能失败；本页在 `npm run dev` 下可验证，`src/pages/Shipment/index.tsx` 无 TS 报错。  
2. **回单校验**：v1 签收不强制回单；若后续版本恢复强制回单，需同步更新本清单与产品说明。  
3. **关闭幂等**：后端对已设置 closedAt 的再次 POST close 返回 200 且不重复写入；前端不依赖该行为做特殊提示。
