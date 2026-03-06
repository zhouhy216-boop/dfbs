# SHIPFLOW-260304-001-05 影响检查（事实摘要）

**Request ID:** SHIPFLOW-260304-001-05-IMP  
**Related:** Step-05（手动标记异常 + 异常记录可见性）

---

## 1) 事实确认（含证据）

**POST /api/v1/shipments/{id}/exception**
- **Payload：** `ReasonRequest { reason: String }` 仅此字段（`ReasonRequest.java`）。Body: `{ "reason": "..." }`；query: `operatorId`。
- **持久化：** 无独立“异常记录”表。仅更新 `shipment` 行：`status = EXCEPTION`，`exception_reason = reason`（`ShipmentService.handleException` → `ShipmentEntity.exceptionReason`，`V0018__shipment_workflow.sql` 增加 `exception_reason TEXT`）。即单次文本，不追加、不记多条。

**其他表/实体**
- **damage_record**（V0020）：按 `shipment_id` + `shipment_machine_id`，记录损坏类型/处理/attachment_urls 等，用途为“损坏记录”，非发运级异常标记。
- **after_sales**（V0038）：按 `source_shipment_id`，售后（换/修）单，含 reason/attachments，与“标记发运异常”流程分离。
- 无现有“发运异常记录”表可复用；若需多条/结构化异常记录需新建表或扩展现有模型。

**“设备列表”入口**
- 后端：`GET /api/v1/shipments/{id}/machines` 返回该发运下的 `ShipmentMachineEntity` 列表（`ShipmentController.getMachines`，注释 “For after-sales machine selector”）。
- 前端：`Shipment/index.tsx` 中“标记异常”为**发运级**操作（Drawer 内“可执行操作”按钮 → 弹窗输入原因 → POST exception）；无“按设备标记异常”的单独入口；机器信息出现在创建/编辑流程（如机器/序列号表单项），非“物流在设备列表上勾选标记异常”的 UI。

**附件机制**
- 发运当前为 URL 字段：`ShipmentEntity.receiptUrl`、`ticketUrl`；通过 ship/tracking/setReceiptUrl 等写入；`AttachmentRuleService` 仅做 URL 校验，无发运维度的通用上传 API。damage_record 有 `attachment_urls`（TEXT），after_sales 有 `attachments`（TEXT），均为各自模块使用。

---

## 2) 可能影响范围

| 区域 | 说明 |
|------|------|
| **Backend** | `ShipmentService.handleException`、`ShipmentController.exception`（payload 若扩展需改 ReasonRequest 或新 DTO）；若增加“异常记录”可见性，需确认 list/detail 是否返回 `exceptionReason`（当前为整实体返回，含该字段）；若 Step-05 引入多条异常记录，需新表/新实体及接口。权限键 `shipment.shipments:EXCEPTION` 已存在并校验。 |
| **Frontend** | `Shipment/index.tsx`：现有“标记异常”弹窗（reason 必填）+ POST exception；`ShipmentDetail` 未声明 `exceptionReason`，Descriptions 未展示异常原因；若“异常记录可见”包含在详情中展示，需增加字段与展示；若需“设备列表上标记”，需在 Drawer 或它处接入 machines 列表及新操作。 |

---

## 3) 回归观察项

- 状态流转不变：仅 CREATED/PENDING_SHIP/SHIPPED 可标记异常（COMPLETED/CANCELLED 已禁止），保持现有 `require()` 逻辑。
- 权限：`shipment.shipments:EXCEPTION` 保持不变；workflow 过滤与前端 `permKeyForAction('EXCEPTION')` 已对接。
- 保持人工录入：无自动检测；异常原因仍为人工填写文本。
- 不破坏售后/损坏：after_sales、damage_record 及各自 API 与发运 exception 解耦；Step-05 若只做“发运级异常 + 可见性”，不直接改这两块。

---

## 4) 构建/测试状态

- **Full-suite build：** 此前执行 `.\mvnw.cmd -q package` 存在既有失败（非发运模块）；`.\mvnw.cmd -q package -DskipTests` 通过；发运相关测试（ShipmentProcessTest、ShipmentPanoramaTest、AttachmentRuleTest、ShipmentControllerPermissionTest）通过。
- **可能涉及测试：** 若扩展 exception 的 payload 或增加异常记录查询，可能触及 `ShipmentProcessTest`、`ShipmentPanoramaTest` 及权限/controller 测试；当前无专门 “exception record visibility” 的自动化用例。
