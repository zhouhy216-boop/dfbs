# SHIPFLOW-260304-001-03 发运单权限控制 — 冒烟/回归检查清单

**Ticket:** SHIPFLOW-260304-001-03.d  
**Purpose:** Step-03（页面 + 操作权限）可被 CEO 快速复验：无 VIEW 不可进页、按钮按权限过滤、服务端 403、workflow 不展示无权限操作。

---

## 前置条件

- **账号与权限键（与后端常量一致）：**
  - 页面/列表/详情/workflow：`shipment.shipments:VIEW`
  - 操作：`shipment.shipments:ACCEPT` / `PREPARE` / `SHIP` / `TRACKING` / `COMPLETE` / `EXCEPTION` / `CANCEL`
- 至少两个测试账号：一个仅有 VIEW（或无任何 shipment 权限），一个具备 VIEW + 部分或全部上述操作权限。
- 后端已部署（ShipmentController 已注入 PermEnforcementService，list/detail/workflow 及各 POST 已做 requirePermission；GET workflow 按 effectiveKeys 过滤 actions）。
- 前端已启动（`npm run dev`；若全量 `npm run build` 因项目内其他 TS 错误失败，可用 dev 验证本页）。

---

## 冒烟步骤（6–12）

| # | 步骤 | 预期结果 |
|---|------|----------|
| 1 | 使用**无** `shipment.shipments:VIEW` 的账号登录，浏览器访问 `/shipments` | 前端在加载权限后重定向到 `/dashboard`，不展示发货列表；可选：state 中带 `reason: 'forbidden'`。 |
| 2 | 使用仅有 VIEW、**无** ACCEPT/SHIP 等操作权限的账号访问 `/shipments` | 可进入页面，列表与详情可正常打开；详情 Drawer 中「可执行操作」区域不展示（或仅展示该账号具备的 action 对应按钮）。 |
| 3 | 使用有 VIEW + ACCEPT 的账号，对一条 CREATED 状态发货单打开详情 | 「可执行操作」中应出现「审核并补充」；无 SHIP 权限时不应出现「发运」。 |
| 4 | 使用有 VIEW + 全部操作权限的账号，对 PENDING_SHIP 打开详情 | 「可执行操作」中应出现「备货确认」「发运」「标记异常」「取消」等（与 backend workflow 输出一致）。 |
| 5 | 使用**无** ACCEPT 权限的账号，直接调用 `POST /api/v1/shipments/{id}/accept`（如用 curl/Postman，同账号 Cookie/Header） | 响应 **403**，响应体含 `machineCode: "PERM_FORBIDDEN"`（或项目约定的 403 结构）。 |
| 6 | 使用**无** SHIP 权限的账号，在 UI 上对 PENDING_SHIP 发货单尝试发运（若按钮被前端隐藏则改用直接调 API） | 若前端已隐藏「发运」按钮，则用户无法点；若仍能发起请求，服务端返回 **403** + PERM_FORBIDDEN。 |
| 7 | 使用有 VIEW 的账号调用 `GET /api/v1/shipments/{id}/workflow`，且该账号**无** SHIP 权限 | 返回的 `actions` 列表中**不**包含 actionCode 为 `SHIP` 的项（后端已按 effectiveKeys 过滤）。 |
| 8 | 使用有 VIEW 的账号，列表或详情/workflow 请求被服务端返回 403（如权限被收回后刷新） | 前端提示「无权限访问发运单」，并跳转至 `/dashboard` 或关闭详情 Drawer。 |
| 9 | 使用有完整 shipment 权限的账号，执行列表、详情、workflow、接单/备货/发运/签收等操作 | 行为与 Step-01/02 一致，无退步；workflow 返回的 actions 与当前权限一致。 |
| 10 | 确认「发起售后」「导出运单/回单」未因 Step-03 变更而要求新权限 | 有发运单访问能力的用户仍可使用上述功能（未新增权限键）。 |

---

## 预期结果汇总

- **无 VIEW → 不可进入 /shipments**：前端在权限加载后判断，无 VIEW 则重定向到 dashboard，与 PlatformViewGuard 模式一致。  
- **按钮按操作权限过滤**：可执行操作区仅展示用户具备对应 permission key 的 action（前端 `visibleActions` 与后端 workflow 过滤双重保障）。  
- **服务端拒绝无权限 POST**：对 accept/prepare/ship/tracking/complete/exception/cancel 等 POST，无对应权限时返回 **403**，body 含 PERM_FORBIDDEN。  
- **Workflow 不宣传无权限操作**：`GET .../workflow` 返回的 `actions` 已按当前用户 effectiveKeys 过滤，前端再按 `has(permKeyForAction)` 过滤展示，避免“有按钮但 403”。

---

## 已知限制（回归观察项，供 CEO）

1. **前端全量构建**：项目内其他文件存在既有 TypeScript 错误，`npm run build` 可能失败；本页在 `npm run dev` 下可验证，且 `src/pages/Shipment/index.tsx` 无 TS 报错。  
2. **权限来源**：当前依赖 `GET /v1/perm/me/effective-keys`（或 test 环境下的 effective-keys）及前端缓存；若权限在会话中途变更，需重新拉取/登出再登入后行为才完全一致。  
3. **创建/导出/机器列表等**：Step-03 仅对 list、detail、workflow 及 8 个步骤 POST 做了权限控制；创建发货单、导出运单/回单、machines 等接口未在本步增加新权限键，保持与现有行为一致。
