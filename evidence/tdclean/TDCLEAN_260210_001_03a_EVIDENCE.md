# TDCLEAN-260210-001-03.a Evidence — Module→tables, must-keep, delete-order

**Request ID:** TDCLEAN-260210-001-03.a-EVID  
**Purpose:** Facts for Step-03 preview mapping. No code modified.

---

## Item 1: Module → candidate DB tables (business test data)

Evidence: `backend/dfbs-app/src/main/resources/db/migration/*.sql`, `backend/dfbs-app/src/main/java/com/dfbs/app/modules/**/*Entity.java` (@Table name).

| moduleId | Candidate tables | Evidence (migration / entity) |
|----------|------------------|-------------------------------|
| dashboard | (none; aggregator) | — |
| quotes | quote, quote_item, quote_version, quote_workflow_history, quote_payment, quote_collector_history, quote_void_request, quote_void_application, quote_number_sequence | V0005,V0006,V0007,V0010,V0011,V0014,V0016; QuoteEntity, QuoteItemEntity, etc. |
| shipments | shipment, shipment_machine | V0017,V0019; ShipmentEntity, ShipmentMachineEntity |
| after-sales | after_sales | V0038; (entity in modules) |
| work-orders | work_order, work_order_record, work_order_part | V0017,V0043; WorkOrderEntity, WorkOrderRecordEntity, WorkOrderPartEntity |
| finance | payment, payment_allocation, account_statement, account_statement_item, invoice_application, invoice_record, invoice_item_ref | V0026,V0034,V0015; PaymentEntity, AccountStatementEntity, etc. |
| warehouse-inventory | warehouse, inventory, inventory_log, transfer_order, special_outbound_request, wh_warehouse, wh_inventory, wh_stock_record | V0022,V0042; WarehouseEntity, WhWarehouseEntity, WhInventoryEntity, etc. |
| warehouse-replenish | wh_replenish_request | V0042; WhReplenishRequestEntity |
| import-center | (no dedicated table; data lands in md_customer, contracts, machines, etc.) | — |
| customers | md_customer, md_customer_alias, md_customer_merge_log | V0004,V0031; CustomerEntity, CustomerAliasEntity, CustomerMergeLogEntity |
| contracts | contracts | V0039; ContractEntity |
| machines | machines, machine_ownership_log | V0039; MachineEntity, MachineOwnershipLogEntity |
| machine-models | machine_models | V0039; MachineModelEntity |
| model-part-lists | model_part_lists, bom_conflicts | V0039,V0040; ModelPartListEntity, BomConflictEntity |
| spare-parts | spare_parts | V0039; SparePartEntity |
| sim-cards | sim_cards, sim_binding_log | V0039; SimCardEntity, SimBindingLogEntity |
| platform-orgs | platform_org, platform_org_customers | V0049,V0051; PlatformOrgEntity, PlatformOrgCustomerEntity |
| platform-applications | platform_account_applications | V0050; PlatformAccountApplicationEntity |
| sim-applications | (same as platform / sim_cards) | — |
| confirmation-center | (temp-pool/smart-select; no dedicated table found in migrations) | Not found |
| platform-config | md_platform | V0060; PlatformConfigEntity @Table(name="md_platform") |
| org-levels | org_level | V0061,V0062; OrgLevelEntity |
| org-tree | org_node, org_person, person_affiliation, job_level, org_position_catalog, org_position_enabled, org_position_binding, org_level_position_template | V0061,V0063,V0064; OrgNodeEntity, OrgPersonEntity, etc. |
| org-change-logs | org_change_log | V0061; OrgChangeLogEntity |

---

## Item 2: Must-keep baseline (Mode A: login + core navigation)

| Table / entity | Why must-keep | Evidence |
|----------------|---------------|----------|
| app_user | Login/auth; roles in authorities | V0024; UserEntity @Table("app_user") |
| permission_request | Permission flow | V0024; PermissionRequestEntity |
| md_platform | Platform config (options/rules for platform UI) | V0060; PlatformConfigEntity |
| org_level | Org-structure config (level config page) | V0061,V0062; OrgLevelEntity |
| org_position_catalog | Position templates (org tree) | V0063; OrgPositionCatalogEntity |
| business_line | Quote CC / notification | V0012; (notification FK) |
| warehouse_config | Quote CC warehouse | V0013 |
| quote_number_sequence | Quote numbering (operational) | V0006 |
| payment_method | Quote payment methods | V0010 |
| fee_category, fee_type | Quote fee dictionary | V0009 |
| damage_type, damage_treatment | Damage config (read-only refs) | V0020 |
| md_carrier, md_carrier_rule | Carrier/config for shipment | V0036 |

Menu/routes: **not stored in DB**; frontend `App.tsx`, `BasicLayout.tsx` (evidence: no table for routes in migrations).

---

## Item 3: Key delete-order dependencies (FK / associations)

Evidence: `db/migration/*.sql` REFERENCES / FOREIGN KEY.

- **quote children before quote:** quote_item, quote_payment, quote_workflow_history, quote_void_request, quote_void_application, quote_collector_history → quote. shipment, work_order → quote. invoice_item_ref → invoice_record → invoice_application; payment_allocation → payment; payment → account_statement. (V0007,V0010,V0011,V0014,V0016,V0017,V0015,V0034,V0026)
- **shipment children before shipment:** shipment_machine, damage_record, freight_bill_item (FK shipment_id) → shipment. (V0019,V0020,V0021)
- **work_order children:** work_order_record(work_order_id), work_order_part(work_order_id) — no FK in migration; logical dependency. (V0043)
- **platform_org_customers → platform_org, md_customer.** platform_org → md_customer. (V0051,V0049)
- **org_structure:** org_position_enabled, org_position_binding → org_node, org_person; person_affiliation → org_person, org_node; org_node → org_level; org_level_position_template → org_level, org_position_catalog. (V0063,V0061)
- **contract_price_item → contract_price_header → md_customer.** (V0035)
- **wh_inventory → wh_warehouse.** inventory → warehouse; transfer_order → warehouse. (V0042,V0022)
- **expense → claim.** (V0032)
- **md_contract → md_customer** (V0004). contracts (V0039) — no FK to md_customer in same table name; separate schema layer.

---

## Not found

- **confirmation-center:** No dedicated "temp pool" or confirmation table found in migrations; may live in application cache or other store (Not verified).
- **import-center:** No dedicated import table; imports write into customers, contracts, machines, etc.
