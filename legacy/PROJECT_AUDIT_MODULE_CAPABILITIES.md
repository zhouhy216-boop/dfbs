# Module Capabilities Audit

**Source**: Codebase `backend/dfbs-app/src/main/java`, `src/test/java`.  
**Date**: 2026-01-31.  
**Scope**: Backend only. "Entrust/Order" = ShipmentType (Sales/Production/Customer Delegate) in Shipment module.

---

## 1. Overview Matrix

| Module | Features (Business Language) | Key APIs (Controller/Method) | Key Tests (Class/Method) | Tables |
| :--- | :--- | :--- | :--- | :--- |
| Customer | Create, List, Search, Merge, Alias | `POST/GET /api/masterdata/customers`, `GET /api/v1/customers`, merge | CustomerMasterDataCreateTest, CustomerMasterDataSearchTest, CustomerMergeTest | md_customer, md_customer_alias, md_customer_merge_log |
| Contract | Create, Search | ContractMasterDataController | ContractMasterDataCreateTest, ContractMasterDataSearchTest | md_contract |
| Product | Create, Search | ProductMasterDataController | ProductMasterDataCreateTest, ProductMasterDataSearchTest | md_product |
| Machine | Create, List, Search | MachineMasterDataController, `/api/v1/machines` | MachineMasterDataCreateTest, MasterDataListTest | md_machine |
| ICCID | Create, List | IccidMasterDataController | IccidMasterDataCreateTest | md_iccid |
| Quote | Create, Version, Status, Items, Submit, Confirm, Cancel, Export | QuoteController, QuoteItemController, QuoteWorkflowController, QuoteExportController | QuoteStateTest, QuoteItemTest, QuoteWorkflowTest, QuoteExportTest | quote, quote_item, quote_version |
| Quote Payment | Submit, Confirm, Batch, by Quote | PaymentController (quote), PaymentRecordTest, QuoteBatchPaymentTest | quote_payment, payment_method, quote_collector_history |
| Quote Void | Apply, Audit, Direct void | QuoteVoidController | QuoteVoidTest | quote_void_request, quote_void_application |
| Work Order → Quote | Create from work order | WorkOrderQuoteController | WorkOrderQuoteTest | work_order |
| Dictionary | Fee categories, fee types, units | DictionaryController | DictionaryLogicTest, DictionaryQuoteTest | fee_type, fee_category |
| Shipment | Normal/Entrust create, Accept, Ship, Complete, Parse text, Infer type | ShipmentController | ShipmentProcessTest, ShipmentPanoramaTest | shipment, shipment_machine |
| Carrier | CRUD, Recommend by address | CarrierController | CarrierTest | md_carrier, md_carrier_rule |
| Freight Bill | Create (by carrier+IDs or by period), Items, Confirm, Settle, Export (HTML + merged Excel) | FreightBillController | FreightBillTest | freight_bill, freight_bill_item |
| Damage | Create, List by shipment, Repair stage, Compensation, Config types/treatments | DamageController | DamageRecordTest | damage_record, damage_type, damage_treatment |
| Expense | Create, Update, Void, List | ExpenseController | ExpenseClaimTest | expense |
| Claim | Submit, Audit, List | ClaimController | ExpenseClaimTest | claim |
| Expense Stats | Group stats, Export Excel | ExpenseStatsController | ExpenseStatsTest | (expense, claim) |
| Payment (standalone) | Create, Confirm, Cancel, Allocation | PaymentController (payment), BindPaymentsRequest | PaymentTest | payment, payment_allocation |
| Statement | Generate, List, Export, Bind payments | AccountStatementController, StatementReconcileController | AccountStatementTest, AccountStatementControllerTest | account_statement, account_statement_item |
| Contract Price (PriceBook) | Create, Update, Deactivate, Suggest price | ContractPriceController | PriceBookTest | contract_price_header, contract_price_item |
| Inventory | Stock, Inbound, Return, Logs | InventoryController | InventoryTest | inventory, inventory_log, warehouse |
| Transfer | Apply, Ship, Receive | TransferController | (covered in InventoryTest / integration) | transfer_order |
| Outbound | WO/Quote/Special outbound, Validate warehouse | OutboundController | (integration) | special_outbound_request |
| Repair | Import, List, Filter, Template | RepairRecordController | RepairRecordTest | repair_record |
| Invoice Application | Submit, Audit, My applications | InvoiceApplicationController | InvoiceApplicationTest | invoice_application, invoice_item_ref, invoice_record |
| Trip Request | Create, Submit, Withdraw, Approve, Reject, Cancel | TripRequestController | TripRequestTest | trip_request |
| Permission Request | Apply, Resubmit, Return, Reject, Approve, My/Pending | PermissionRequestController | PermissionRequestTest | permission_request |
| Notification | List, My, Unread, Read, Read-all, Preference | NotificationController | NotificationTest | notification |
| BOM (machine) | Import, By machine active/history, Version items | BomController | BomServiceTest | bom_version, bom_item |
| Part / Product BOM | List, Search, Import, BOM by product | PartController | QuotePartLinkTest, MasterDataListTest | part, product_bom |
| Settings | Business line, Warehouse config | BusinessLineController, WarehouseConfigController | (no dedicated test) | business_line, warehouse_config |
| Attachment (rules) | Validate mandatory attachments by target/point | AttachmentRuleService (used by services) | AttachmentRuleTest | (no table; rule config) |
| User | (auth / app_user) | (Spring Security) | — | app_user |

---

## 2. Module Details

### Customer
* **Features**: Create, get, update name, soft delete; merge with field overrides; search (keyword, pageable); alias support.
* **Gaps**: No dedicated test for alias-only flows; merge undo covered in CustomerMergeTest.

### Contract
* **Features**: Create, get by id, update name, soft delete; search (keyword, pageable).
* **Gaps**: Contract price linkage is in ContractPrice module; no contract-specific workflow test.

### Product
* **Features**: Create, get, update, soft delete; search.
* **Gaps**: None for MVP scope.

### Machine / ICCID
* **Features**: Create, list, keyword search (Machine/ICCID list + search per milestone).
* **Gaps**: None for MVP; MasterDataListTest covers list/search.

### Quote
* **Features**: Create, update, confirm, cancel; version activate; items CRUD, auto amount; workflow submit/audit/assign-collector/fallback; export Excel/PDF; from-workorder; dictionary validation; warehouse reminder.
* **Gaps**: QuoteStandardizationTest, QuoteCcTest, QuoteWarehouseTest, QuoteDownstreamTest cover specific flows; no single “full lifecycle” E2E test.

### Quote Payment / Payment (standalone)
* **Features**: Submit payment, confirm, batch; payment by quote; standalone payment create/confirm/cancel; allocation (bind to statement, cross-period).
* **Gaps**: PaymentTest covers allocation and quote-state drive; no separate “allocation only” test.

### Quote Void
* **Features**: Apply, audit, direct void; history/requests by quote.
* **Gaps**: QuoteVoidTest covers main paths.

### Shipment
* **Features**: Normal create, Entrust create (customer/sales delegate); accept, ship, complete, exception, cancel; parse text; infer type + carrier recommend; machines update; export ticket/receipt.
* **Gaps**: ShipmentType inferred and billable logic tested in CarrierTest; no dedicated “entrust-only” integration test.

### Carrier
* **Features**: CRUD; recommend by address (rule keyword match, priority).
* **Gaps**: No CRUD test; CarrierTest covers recommend + type inference + billable.

### Freight Bill
* **Features**: Create by carrier+shipmentIds or by carrierId+period; update items; remove shipment; confirm (attachment); settle; export draft HTML; export merged Excel (Summary + Details).
* **Gaps**: None for MVP; FreightBillTest covers cycle, remove, lock, export merged.

### Damage
* **Features**: Create, list by shipment; update repair stage, compensation; config types/treatments.
* **Gaps**: DamageRecordTest; no config CRUD test.

### Expense / Claim
* **Features**: Expense create/update/void; Claim submit/audit; stats + export Excel.
* **Gaps**: ExpenseClaimTest, ExpenseStatsTest; no expense-only lifecycle test.

### Statement
* **Features**: Generate, list, export, bind payments (reconcile).
* **Gaps**: AccountStatementTest, AccountStatementControllerTest; bind-payments covered.

### Contract Price (PriceBook)
* **Features**: Header/item CRUD, deactivate; suggest price by customer/doc date/item type; quote item auto-suggest (pre-submit); manual price reason.
* **Gaps**: PriceBookTest; no “multi-contract lowest price” E2E test.

### Inventory / Transfer / Outbound
* **Features**: Stock query, inbound, return, logs; transfer apply/ship/receive; outbound WO/quote/special; validate warehouse.
* **Gaps**: InventoryTest; no dedicated transfer or outbound controller test.

### Repair
* **Features**: Import Excel, list with filter, template download.
* **Gaps**: RepairRecordTest; no repair–quote linkage test.

### Invoice Application
* **Features**: Submit, audit, my applications, get by id.
* **Gaps**: InvoiceApplicationTest; no invoice record generation test.

### Trip Request
* **Features**: Full lifecycle (create, submit, withdraw, leader/finance approve, return, reject, cancel request/approve/reject).
* **Gaps**: TripRequestTest.

### Permission Request
* **Features**: Apply, resubmit, return, reject, approve, my-requests, pending.
* **Gaps**: PermissionRequestTest.

### Notification
* **Features**: List, my, unread, unread-count, read, read-all, preference.
* **Gaps**: NotificationTest.

### Part / BOM
* **Features**: Part list/search/import; product BOM; BOM import (machine); active/history by machine.
* **Gaps**: QuotePartLinkTest, BomServiceTest, MasterDataListTest; no Part CRUD-only test.

### Settings
* **Features**: Business line, warehouse config APIs.
* **Gaps**: No dedicated test.

### Attachment Rules
* **Features**: Validate mandatory attachments by target type and point (e.g. freight bill confirm).
* **Gaps**: AttachmentRuleTest.

---

**Modules counted (distinct business areas)**: 28 (Customer, Contract, Product, Machine, ICCID, Quote, Quote Payment, Quote Void, Work Order, Dictionary, Shipment, Carrier, Freight Bill, Damage, Expense, Claim, Expense Stats, Payment, Statement, Contract Price, Inventory, Transfer, Outbound, Repair, Invoice, Trip Request, Permission, Notification, BOM, Part, Settings, Attachment).
