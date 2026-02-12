# TDCLEAN-260210-001-05.a Attachments evidence (DB + file objects)

**Request ID:** TDCLEAN-260210-001-05.a-EVID  
**Purpose:** Facts for Step-05 attachments deletion (DB + file objects). No code modified.

---

## 1) Attachment DB schema

**Finding:** There is **no dedicated attachment or file_object table**. Attachment data is stored as **columns on business tables** (URL/path or TEXT/JSON).

**Evidence:** Flyway migrations + JPA entities (`@Table`, `@Column`).

| Table | Column(s) | Type | Evidence (migration / entity) |
|-------|-----------|------|-------------------------------|
| work_order_record | attachment_url | VARCHAR(512) | V0043; WorkOrderRecordEntity |
| after_sales | attachments | TEXT | V0038; AfterSalesEntity (JSON array of URLs) |
| contracts | attachment | TEXT NOT NULL | V0039; ContractEntity (URL or JSON) |
| quote_payment | attachment_urls | VARCHAR(2000) | V0010; QuotePaymentEntity (JSON or comma-separated) |
| quote_void_application | attachment_urls | VARCHAR(2000) | V0011; QuoteVoidApplicationEntity |
| damage_record | attachment_urls | TEXT NOT NULL | V0020; DamageRecordEntity |
| freight_bill | attachment_url | VARCHAR(512) | V0021; FreightBillEntity |
| shipment | receipt_url, ticket_url | VARCHAR(512) each | V0017/shipment; ShipmentEntity |

**Key columns (per business table):** id (PK on each table), business ref is the row itself; file reference is the URL/path string in the column(s) above. **No** `storage_type` or `deleted_flag` column for attachments in these tables (soft delete exists only on some entities, e.g. customer `deletedAt`, not on attachment columns).

---

## 2) Storage backend

**Finding:** **Mock only** — no real file persistence. Upload returns a **mock URL**; no local filesystem path, no S3/OSS/MinIO, no DB blob.

**Evidence:**
- `application/attachment/AttachmentUploadService.java`: "MVP: mock storage returns a mock URL." Builds URL as `mockBaseUrl + "/" + attachmentType.name().toLowerCase() + "/" + UUID.randomUUID() + ext`; does **not** write file to disk or call object storage.
- Config: `@Value("${dfbs.attachment.mock-base-url:/uploads}")` → **config key** `dfbs.attachment.mock-base-url`, default **`/uploads`**. Not present in `application.yml` (so default used).
- No other storage client or upload directory in `application.yml` or in code (no S3, OSS, MinIO, or `file.upload-dir`).

**Storage backend type:** Mock URL only (no physical file storage).  
**Config key / path:** `dfbs.attachment.mock-base-url`, default `/uploads` (path is URL path, not filesystem).

---

## 3) Delete mechanics

**Finding:** **No existing service for deleting attachment file objects.** No batch delete, no soft/hard delete for files.

**Evidence:**
- `AttachmentUploadService`: only `upload(MultipartFile, AttachmentType)`; no `delete`, `deleteByUrl`, or batch method.
- `AttachmentRuleService`: validation only (`validate(...)`); no delete.
- Controllers/services that **set** attachment URLs (e.g. DamageService, QuotePaymentService, FreightBillService, WorkOrderService) do not perform file deletion; they only update DB columns.
- No dedicated "attachment delete" or "file cleanup" service in `application/attachment` or elsewhere.

**Implication for Step-05:** Any attachment "deletion" would need to (a) clear or null the URL/path columns in the listed business tables (DB), and (b) if/when real file storage is added, delete physical files by URL/path — **no existing entry point** for (b). Today, with mock storage, there are no physical files to delete.

---

## 4) Safety + reporting implications

**What can fail (once real storage exists):** Missing file (already deleted or wrong path), permissions (filesystem or object storage), network (object storage unreachable). Errors should be surfaced in the same style as execute report (e.g. per-table or per-module status: SUCCESS/FAILED, error message).

**Retry/queue:** **No** RabbitMQ or other message queue/retry in the project (grep: no RabbitMQ, retry, queue usage in backend). Must NOT introduce RabbitMQ by default for attachment delete.

**Existing behavior:** Upload size validated (max 10MB); AttachmentRuleService validates required attachments and max 10 URLs per batch. No retry on upload.

---

## Short receipt

- **Completed?** Yes.
- **DB tables (compact):** No dedicated attachment table. Attachment **columns** live on: work_order_record (attachment_url), after_sales (attachments), contracts (attachment), quote_payment (attachment_urls), quote_void_application (attachment_urls), damage_record (attachment_urls), freight_bill (attachment_url), shipment (receipt_url, ticket_url).
- **Storage backend:** Mock only. Config key `dfbs.attachment.mock-base-url` (default `/uploads`). No filesystem path, no S3/OSS/DB blob.
- **Existing delete service:** None. AttachmentUploadService has upload only; no delete or batch delete for files.
- **Known failure modes:** When real storage exists: missing file, permissions, network. No retry/queue (no RabbitMQ).
- **File saved:** `evidence/tdclean/evidence/TDCLEAN_260210_001_05a_attachments_evidence.md`.
- **Full-suite build:** Not run (evidence-only).
- **Blocker question:** None.
