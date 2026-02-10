# DATA_BASELINE — Flyway migrations and key tables

- **As-of:** 2026-02-09 14:00
- **Repo:** main
- **Commit:** 1df603c5
- **Verification method:** list_dir `backend/dfbs-app/src/main/resources/db/migration/`; grep `CREATE TABLE` in each migration file.

**Facts only.** Location: `backend/dfbs-app/src/main/resources/db/migration/`. Format: `Vxxxx__<name>.sql`.

---

## Migration filenames (V0001–V0066)

| Id | Flyway filename |
|----|-----------------|
| V0001 | V0001__init.sql |
| V0002 | V0002__quote_version.sql |
| V0003 | V0003__quote_version_only_one_active.sql |
| V0004 | V0004__masterdata_init.sql |
| V0005 | V0005__quote_item.sql |
| V0006 | V0006__quote_header_and_sequence.sql |
| V0007 | V0007__quote_item_mvp.sql |
| V0008 | V0008__quote_workorder_fields.sql |
| V0009 | V0009__fee_dictionary_and_bom.sql |
| V0010 | V0010__payment_record.sql |
| V0011 | V0011__quote_void_process.sql |
| V0012 | V0012__quote_cc_leadership.sql |
| V0013 | V0013__quote_cc_warehouse.sql |
| V0014 | V0014__quote_payment_workflow.sql |
| V0015 | V0015__invoice_workflow.sql |
| V0016 | V0016__quote_void_process.sql |
| V0017 | V0017__quote_downstream.sql |
| V0018 | V0018__shipment_workflow.sql |
| V0019 | V0019__shipment_panorama.sql |
| V0020 | V0020__damage_record.sql |
| V0021 | V0021__freight_bill.sql |
| V0022 | V0022__inventory_mvp.sql |
| V0023 | V0023__repair_ledger.sql |
| V0024 | V0024__permission_request.sql |
| V0025 | V0025__notification_center.sql |
| V0026 | V0026__account_statement.sql |
| V0027 | V0027__bom_versioning.sql |
| V0028 | V0028__quote_part_link.sql |
| V0029 | V0029__machine_iccid_list_support.sql |
| V0030 | V0030__quote_standardization.sql |
| V0031 | V0031__customer_merge_support.sql |
| V0032 | V0032__expense_claim_mvp.sql |
| V0033 | V0033__trip_request_mvp.sql |
| V0034 | V0034__payment_allocation_mvp.sql |
| V0035 | V0035__contract_pricing_mvp.sql |
| V0036 | V0036__carrier_freight_bill_mvp.sql |
| V0037 | V0037__correction_mvp.sql |
| V0038 | V0038__after_sales_mvp.sql |
| V0039 | V0039__masterdata_six_pack.sql |
| V0040 | V0040__bom_conflicts_and_spare_part_aliases.sql |
| V0041 | V0041__quote_payment_created_at.sql |
| V0042 | V0042__warehouse_mvp.sql |
| V0043 | V0043__work_order_service_flow.sql |
| V0044 | V0044__work_order_cancellation_reason.sql |
| V0045 | V0045__work_order_refinement.sql |
| V0046 | V0046__app_user_username.sql |
| V0047 | V0047__app_user_seed_admin.sql |
| V0048 | V0048__smart_select_infrastructure.sql |
| V0049 | V0049__platform_org_master.sql |
| V0050 | V0050__platform_account_applications.sql |
| V0051 | V0051__refactor_platform_org.sql |
| V0052 | V0052__platform_application_source_and_customer_name.sql |
| V0053 | V0053__relax_application_constraints.sql |
| V0054 | V0054__enforce_org_uniqueness.sql |
| V0055 | V0055__org_source_tracking.sql |
| V0057 | V0057__drop_data_governance_task.sql |
| V0058 | V0058__add_reject_reason.sql |
| V0059 | V0059__platform_org_status.sql |
| V0060 | V0060__create_md_platform.sql |
| V0061 | V0061__org_structure_v1.sql |
| V0062 | V0062__org_level_default_seed.sql |
| V0063 | V0063__org_position_config_v1.sql |
| V0064 | V0064__org_position_catalog_and_templates_seed.sql |
| V0065 | V0065__org_level_order_index_unique.sql |
| V0066 | V0066__org_level_order_index_repair.sql |

(No V0056 in repo.)

---

## Key tables/entities by migration (from migration file content)

| Migration | Key tables touched | Pointer |
|-----------|--------------------|---------|
| V0001 | platform_bootstrap | V0001__init.sql |
| V0002 | quote_version | V0002__quote_version.sql |
| V0003 | (constraint only) | V0003__quote_version_only_one_active.sql |
| V0004 | md_customer, md_contract, md_product, md_machine, md_iccid | V0004__masterdata_init.sql |
| V0005 | quote_item | V0005__quote_item.sql |
| V0006 | quote_number_sequence, quote | V0006__quote_header_and_sequence.sql |
| V0007–V0008 | quote_item, workorder fields | V0007, V0008 |
| V0009 | fee_category, fee_type, part, product_bom | V0009__fee_dictionary_and_bom.sql |
| V0010 | payment_method, quote_payment, quote_collector_history | V0010__payment_record.sql |
| V0011 | quote_void_application | V0011__quote_void_process.sql |
| V0012 | business_line, notification | V0012__quote_cc_leadership.sql |
| V0013 | warehouse_config | V0013__quote_cc_warehouse.sql |
| V0014 | quote_workflow_history | V0014__quote_payment_workflow.sql |
| V0015 | invoice_application, invoice_record, invoice_item_ref | V0015__invoice_workflow.sql |
| V0016 | quote_void_request | V0016__quote_void_process.sql |
| V0017 | shipment, work_order | V0017__quote_downstream.sql |
| V0018–V0019 | shipment_workflow, shipment_machine | V0018, V0019 |
| V0020 | damage_type, damage_treatment, damage_record | V0020__damage_record.sql |
| V0021 | freight_bill, freight_bill_item | V0021__freight_bill.sql |
| V0022 | warehouse, inventory, inventory_log, transfer_order, special_outbound_request | V0022__inventory_mvp.sql |
| V0023 | repair_record | V0023__repair_ledger.sql |
| V0024 | app_user, permission_request | V0024__permission_request.sql |
| V0025 | (notification_center) | V0025__notification_center.sql |
| V0026 | account_statement, account_statement_item | V0026__account_statement.sql |
| V0027 | bom_version, bom_item | V0027__bom_versioning.sql |
| V0028 | (quote_part_link) | V0028__quote_part_link.sql |
| V0029 | (machine_iccid) | V0029__machine_iccid_list_support.sql |
| V0030–V0031 | (quote_standardization), md_customer_alias, md_customer_merge_log | V0030, V0031 |
| V0032 | expense, claim | V0032__expense_claim_mvp.sql |
| V0033 | trip_request | V0033__trip_request_mvp.sql |
| V0034 | payment, payment_allocation | V0034__payment_allocation_mvp.sql |
| V0035 | contract_price_header, contract_price_item | V0035__contract_pricing_mvp.sql |
| V0036 | md_carrier, md_carrier_rule | V0036__carrier_freight_bill_mvp.sql |
| V0037 | correction_record | V0037__correction_mvp.sql |
| V0038 | after_sales | V0038__after_sales_mvp.sql |
| V0039 | contracts, machine_models, spare_parts, model_part_lists, machines, sim_cards, machine_ownership_log, sim_binding_log | V0039__masterdata_six_pack.sql |
| V0040 | bom_conflicts | V0040__bom_conflicts_and_spare_part_aliases.sql |
| V0041–V0042 | (quote_payment_created_at), wh_warehouse, wh_inventory, wh_stock_record, wh_replenish_request | V0041, V0042 |
| V0043 | work_order_record, work_order_part | V0043__work_order_service_flow.sql |
| V0044–V0048 | work_order, app_user, smart_select | V0044–V0048 |
| V0049 | platform_org | V0049__platform_org_master.sql |
| V0050 | platform_account_applications | V0050__platform_account_applications.sql |
| V0051 | platform_org_customers | V0051__refactor_platform_org.sql |
| V0052–V0060 | (platform_application_source, constraints, org_source_tracking, drop_data_governance_task, add_reject_reason, platform_org_status), md_platform | V0052–V0060 |
| V0061 | org_level, org_node, job_level, org_person, person_affiliation, org_change_log | V0061__org_structure_v1.sql |
| V0062 | (org_level seed) | V0062__org_level_default_seed.sql |
| V0063 | org_position_catalog, org_position_enabled, org_position_binding, org_level_position_template | V0063__org_position_config_v1.sql |
| V0064–V0066 | (catalog/templates seed), (order_index unique/repair) | V0064–V0066 |

---

## Not verified

- V0007, V0008, V0025, V0028, V0029, V0030, V0041, V0044–V0048, V0052–V0059, V0062, V0064–V0066: key tables taken from filename or partial read; not every migration file opened for full table list.
