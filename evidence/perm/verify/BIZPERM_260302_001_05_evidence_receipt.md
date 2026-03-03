# BIZPERM-260302-001-05-EVID — Import/Export 业务模块目录 证据收条

**Request ID:** BIZPERM-260302-001-05-EVID  
**Purpose:** 为「目录导入/导出（round-trip、表格化）」收集仓库事实，只读，未修改任何代码。

---

## Completed?

Yes

---

## Findings

**Item 1 — 现有导入/导出与文件上传**

- **导入（Excel + MultipartFile）**：`ImportController.java`（`/api/v1/.../import/*`）使用 `@RequestParam("file") MultipartFile file`，委托各 `*ImportService`（Customer / Contract / Model / SparePart / Machine / SimCard / ModelPartList）；`RepairRecordController.importExcel`、`BomController`、`PartController` 同样使用 `MultipartFile`。多处使用 **EasyExcel.read(file, RowClass, ReadListener)** 做导入。
- **导出（Excel 下载）**：`FreightBillService`、`ExpenseExportService` 使用 **EasyExcel.write** 多 Sheet；`QuoteExportService` 使用 **Apache POI XSSFWorkbook** + 模板 `templates/excel/quote_template_v3.xlsx`；`AccountStatementService` 使用 **XSSFWorkbook** 写 xlsx；`RepairRecordController.downloadTemplate()` 返回 `ResponseEntity<byte[]>`，`Content-Disposition` 形式见 `ExpenseExportService`：`response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")`；`FreightBillController.exportMerged` / `export`、`ShipmentController.exportTicket` / `exportReceipt`、`QuoteExportController.export`、`AccountStatementController.export` 均为 `ResponseEntity<byte[]>` + headers。
- **上传通用**：`AttachmentController.upload` + `AttachmentUploadService.upload(MultipartFile, AttachmentType)`，含大小校验；无 CSV 专用端点。

**Item 2 — 表格解析/生成依赖**

- **backend pom.xml**：已存在 **Apache POI** `poi` + `poi-ooxml` 5.2.5；**EasyExcel** 3.3.2（注释为 “repair ledger”）；**spire.xls.free** 3.9.1。无 opencsv。
- **实际使用**：导入以 **EasyExcel** 为主（Customer/Contract/Model/SparePart/Machine/SimCard/ModelPartList/BOM/RepairRecord 等）；导出兼有 **EasyExcel**（FreightBill/Expense）与 **POI XSSFWorkbook**（Quote/AccountStatement）；RepairRecord 模板下载为 EasyExcel 写出的 byte[]。
- **安全**：Attachment 有文件大小校验；Excel 解析未发现显式“禁止公式/宏”的集中配置，可沿用现有 Import 的流式 ReadListener 与大小限制。

**Item 3 — 目录数据模型（round-trip 需覆盖）**

- **biz_perm_catalog_node**（V0080）：`id` (BIGSERIAL PK)、`parent_id` (FK self, nullable)、`cn_name` (VARCHAR 128 NOT NULL)、`sort_order` (INT NOT NULL DEFAULT 0)、`created_at`、`updated_at`。完整导出需包含 id/parent_id/cn_name/sort_order 以还原树与顺序。
- **biz_perm_operation_point**（V0080）：`id` (BIGSERIAL PK)、`node_id` (FK node, nullable → 未归类)、`permission_key` (VARCHAR 128 UNIQUE)、`cn_name` (VARCHAR 128 nullable)、`sort_order` (INT NOT NULL DEFAULT 0)、`handled_only` (BOOLEAN NOT NULL DEFAULT false)、`created_at`、`updated_at`。完整导出需 node_id(空表示未归类)、permission_key、cn_name、sort_order、handled_only；id 在导入时可用于“按 id 映射”或弃用由 DB 重新生成。

**Item 4 — 校验与冲突策略（事实 + 建议）**

- **permission_key 重复**：DB 唯一约束 `uk_biz_perm_operation_point_key`；现有 `upsertOpPoint`/`claimOpPoints` 按 key 做 upsert。建议：导入时同一文件内重复 key 报错或取最后一行；与库内已有 key 的冲突采用“按 key 更新”或“跳过/报错”二选一并统一。
- **节点 parent 成环 / 自引用**：`BizPermCatalogService.updateNode` 已校验 `parentId != id` 且 `!isDescendant(parentId, id)`；`deleteNode` 禁止有子节点或操作点的节点删除。建议：导入时先建节点再解析 parent_id（或用临时 id/行号映射），并做 cycle 检测或复用现有 isDescendant 逻辑。
- **未知 permission_key**：`PermPermissionTreeService.getAllPermissionKeys()` 为权限宇宙；`BizPermCatalogService` 在 upsertOpPoint/claimOpPoints 中校验 key 在宇宙内，否则 `BIZPERM_PERMISSION_KEY_NOT_FOUND`。建议：导入时对每条 op 校验 key 在 universe，否则 400 或收集错误批量返回。
- **未归类在文件中的表示**：`node_id` 为空即未归类；文件中可用空列或占位符 “未归类”/“” 表示 node_id 为空，导入时写 `node_id = null`。

**Item 5 — 文件格式建议（基于证据）**

- **推荐 xlsx**：与现有 Import/Export 一致（EasyExcel/POI），多 Sheet 可区分节点与操作点，避免单 CSV 多表歧义。
- **xlsx 建议结构**：  
  - **Sheet “节点”**：列 `id, parent_id, cn_name, sort_order`（导出用 id 便于 round-trip；导入时 id 可选，若提供则用于 parent_id 引用，否则由后端生成）。  
  - **Sheet “操作点”**：列 `id, node_id, permission_key, cn_name, sort_order, handled_only`；`node_id` 空表示未归类。  
- **若用单 CSV**：需约定两段（如用空行或表头 “节点”/“操作点” 分隔），或两文件（nodes.csv / op_points.csv）；树结构依赖 parent_id 引用导出 id，round-trip 时需稳定 id 或导入阶段生成 id 映射表。

---

## Not found

- 仓库内无现成的 CSV 导入/导出工具（无 opencsv）；CSV 若采用需自解析或引入轻量库。
- 无 BizPerm Catalog 专用的 export/import 端点；需新增。

---

## Build status

Not required (evidence-only).

---

## Blocker question

None.
