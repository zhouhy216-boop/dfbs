# Platform Application Flow — Rules Report

**Document purpose:** Authoritative, human-readable description of the **current** Platform Application Flow behaviour as implemented in the frontend. Intended as source-of-truth input for a future “form + button configurable” foundation.

**Last extracted from code:** 2026-01 (VNX-20260208-* scope).

---

## 0. Coverage boundary

“Platform Application Flow” includes all of the following, as implemented in the frontend:

| Area | Description |
|------|-------------|
| **Initiation sources** | Sales apply (销售申请), Service apply (服务申请), Enterprise apply (营企申请/申请平台) |
| **Stages** | Create (sales/service/enterprise), Enterprise Confirm (营企确认/营企处理), Enterprise direct create-org (新建机构 duplicate flow) |
| **Duplicate-warning (重复提醒)** | Modal behaviour: button visibility, selection, outcomes (返回编辑, 确认新增, 申请复用, 开卡申请, 申请核查) |
| **Applications management (申请管理)** | Pending vs History tabs; status partition mapping; list row actions (详情, 营企处理, 管理员审核) |
| **Placeholder outcomes** | 开卡申请 → sim-activation placeholder; 申请核查 → verification placeholder; 申请复用 → reuse placeholder |

**Out of scope for this report:** Backend API contracts, Platform Config CRUD (系统/平台配置), SIM管理 (/platform/sim-applications), and any flows not reachable from the above.

---

## 1. Node–Button matrix

**Visibility condition syntax (example):**

```
Visible = (A) Node = "重复提醒" AND (C) Stage = "service" AND (D) violatesHouseRules = true
```

Dimensions: **A** Node/State, **B** Role, **C** Document/Order type (source/stage), **D** Key fields/state, **E** Amount/Quantity thresholds (none in code; see §E in appendix).

For each workflow node/state, buttons and their visibility conditions are defined using:

- **A) Node/State** — which screen or modal
- **B) Role** — not used in code for button visibility (no role checks in frontend for these buttons)
- **C) Document/Order type (source)** — e.g. `sourceType`: FACTORY | SERVICE; or “from create” vs “from 营企确认”
- **D) Key fields / state** — e.g. `violatesHouseRules`, `selectedHitCanReuse`, `pendingCreateValues == null`
- **E) Amount/Quantity thresholds** — **None implemented in code** (no amount/quantity gates on button visibility; see Code evidence §E).

Syntax: visibility = AND of conditions unless stated as “OR”.

---

### 1.1 Duplicate-warning modal (重复提醒)

| Node/State | Button (exact UI label) | Visibility condition |
|------------|-------------------------|----------------------|
| 重复提醒 | 返回编辑 | Always shown. (A) Node = Duplicate-warning modal. |
| 重复提醒 | 确认新增 | (A) Node = Duplicate-warning modal **AND** (D) `!violatesHouseRules`. Hidden when house rules violated. |
| 重复提醒 | 申请复用 | (A) Node = Duplicate-warning modal **AND** (D) `selectedHitCanReuse` (selected hit status is 已禁用 OR 已欠费). |
| 重复提醒 | 开卡申请 | (A) Node = Duplicate-warning modal **AND** (C) Enterprise Confirm stage (duplicate opened from 营企确认, i.e. `pendingCreateValues == null`) **AND** (D) `violatesHouseRules`. |
| 重复提醒 | 申请核查 | (A) Node = Duplicate-warning modal **AND** (C) Service stage (`pendingCreateValues.sourceType === 'SERVICE'`) **AND** (D) `violatesHouseRules`. |

**House rules (violatesHouseRules):** True when platform config has the corresponding rule enabled **and** at least one duplicate hit matches that dimension:

- `ruleUniqueEmail` AND any hit `matchReason` includes “邮箱” or “Email”
- OR `ruleUniquePhone` AND any hit `matchReason` includes “电话” or “Phone”
- OR `ruleUniqueOrgName` AND any hit `matchReason` includes “全称” or “Full Name”

**selectedHitCanReuse:** True when the **selected** hit’s platform status (from `getPlatformStatusDisplay`) is 已禁用 or 已欠费. First hit is auto-selected when modal opens.

**Stage passed to duplicate modal:**

- Create flow (sales): `stage = 'sales'` when `pendingCreateValues != null` and `sourceType === 'FACTORY'`.
- Create flow (service): `stage = 'service'` when `pendingCreateValues != null` and `sourceType === 'SERVICE'`.
- 营企确认 flow: `stage = 'enterprise_confirm'` when `pendingCreateValues == null`.
- Enterprise direct (Org create): `stage = 'enterprise_direct'` (fixed in Org page).

There is **no** “取消” button in the duplicate-warning footer; 返回编辑 (and modal X) close the modal and return to the form without closing the form.

---

### 1.2 Application list (申请管理 — 待处理 tab)

| Node/State | Button / link (exact UI label) | Visibility condition |
|------------|--------------------------------|----------------------|
| 申请管理 · 待处理 | 详情 | Always. (A) Node = Application list row. |
| 申请管理 · 待处理 | 营企处理 | (A) Application list row **AND** (D) `row.status` IN (`PENDING_PLANNER`, `PENDING_CONFIRM`, `CLOSED`). |
| 申请管理 · 待处理 | 管理员审核 | (A) Application list row **AND** (D) `row.status === 'PENDING_ADMIN'`. |

---

### 1.3 Platform Management (平台管理) — Initiation

| Node/State | Button (exact UI label) | Visibility condition |
|------------|-------------------------|----------------------|
| 平台管理 | 销售申请 | Always (toolbar). Opens apply form with source=sales. |
| 平台管理 | 服务申请 | Always (toolbar). Opens apply form with source=service. |
| 平台管理 | 营企申请 | Always (toolbar). Opens apply form with source=enterprise. |
| 平台管理 | 新建机构 | Always (toolbar). Opens create-org modal (duplicate-check and draft supported). |

---

### 1.4 Create apply form (ModalForm — sales/service/enterprise)

| Node/State | Button (exact UI label) | Visibility condition |
|------------|-------------------------|----------------------|
| 销售申请 / 服务申请 / 申请平台 | 提交 (modal ok) | Always when form valid. |
| 销售申请 / 服务申请 / 申请平台 | 保存草稿 | Always. On click: save draft, success message, close modal (and if createModalOnly, navigate to /platform/orgs). |
| 销售申请 / 服务申请 / 申请平台 | 恢复草稿 / 清除草稿 | Shown when `hasDraft` for the form’s draft key (e.g. platform-apply-FACTORY, platform-apply-SERVICE, platform-apply-FACTORY-enterprise). |

---

### 1.5 Enterprise Confirm modal (营企确认)

| Node/State | Button (exact UI label) | Visibility condition |
|------------|-------------------------|----------------------|
| 营企确认 | 关闭申请 | Always (footer). |
| 营企确认 | 取消 | Always (footer). |
| 营企确认 | 保存草稿 | Always (footer). On click: save planner draft, success message, close modal. |
| 营企确认 | 提交至管理员 | Always (footer). Submits planner form; on duplicate, opens duplicate-warning modal. |

---

### 1.6 Admin audit modal (管理员审核)

| Node/State | Button (exact UI label) | Visibility condition |
|------------|-------------------------|----------------------|
| 管理员审核 | (Modal OK) | Approve with orgCodeShort + region required. No role-based hide in code. |

---

### 1.7 Placeholder pages (outcomes of duplicate-warning or navigation)

| Node/State | Description | Outcome |
|------------|-------------|---------|
| 开卡申请 | From duplicate modal when Enterprise Confirm + house rules violated. | Navigate to `/platform/applications/sim-activation` → **placeholder** “网卡开通申请 / 功能开发中，敬请期待。” |
| 申请核查 | From duplicate modal when Service + house rules violated. | **Placeholder** “申请核查 / 功能开发中，敬请期待。” (message.info in modal; route `/platform/applications/verification` exists). |
| 申请复用 | From duplicate modal when selected hit 已禁用 or 已欠费. | **Placeholder** “缴费复用申请 / 功能开发中，敬请期待。” (message.info in modal; route `/platform/applications/reuse` exists). |

---

## 2. Status and state transitions

### 2.1 Application status partition (申请管理 — 待处理 vs 申请历史)

Frontend mapping only (no backend change). Used to filter or partition the application list.

| Partition | Status values (code) |
|-----------|----------------------|
| **待处理** | DRAFT, PENDING, PENDING_PLANNER, PENDING_CONFIRM, PENDING_ADMIN, REJECTED |
| **申请历史** | APPROVED, CLOSED |

Display labels (STATUS_MAP): DRAFT→草稿, PENDING→待申请人, PENDING_PLANNER→待确认, PENDING_CONFIRM→待营企确认, PENDING_ADMIN→待审核, APPROVED→已通过, REJECTED→已驳回, CLOSED→已关闭.

### 2.2 Platform org status (duplicate-warning and org list)

| API / source | Display label |
|--------------|----------------|
| status === 'ARREARS' | 已欠费 |
| status === 'DELETED' \|\| !isActive | 已禁用 (or 已删除 in org detail when DELETED) |
| else | 已启用 |

Used in: HitAnalysisPanel (`getPlatformStatusDisplay`), org list/detail/edit (`getPlatformStatusLabelForOrg`), and for 申请复用 visibility (`canApplyReuseForHit` → 已禁用 or 已欠费).

### 2.3 Duplicate-warning modal

- **Open:** From create submit or 营企确认 submit when check-duplicates returns matches.
- **Close:** 返回编辑 or modal X → close modal only; form (create or 营企确认) stays open.
- **Confirm 确认新增:** From create → doCreate then close modals; from 营企确认 → doPlannerSubmit then close. Draft cleared on success.

---

## 3. Validation and house rules

### 3.1 House rules (duplicate-warning)

- **Source:** Platform config per platform code: `ruleUniqueEmail`, `ruleUniquePhone`, `ruleUniqueOrgName` (booleans).
- **Effect:** If any enabled rule is violated by the duplicate hit set, `violatesHouseRules === true`: 确认新增 is hidden; 开卡申请 (enterprise-confirm) or 申请核查 (service) may be shown.

### 3.2 Create apply form (sales/service/enterprise)

- **Required:** platform, customerName (or customerId), orgFullName, contactPerson, phone, email.
- **Sales (FACTORY):** contractNo required (except enterpriseDirect).
- **Service (SERVICE):** price, quantity, reason required.
- **Enterprise direct:** contractNo not required; salesPerson optional (no error when absent).

### 3.3 Org create (新建机构)

- **Required:** platform, customer (customerId/customerName), orgCodeShort (by platform codeValidatorType), orgFullName, contactPerson, contactPhone, contactEmail, salesPerson (申请人).
- **Default:** isActive = true.
- **Duplicate check:** Before submit; on matches, show duplicate-warning modal (enterprise_direct stage). Draft key: `platform-org-create-admin`.

### 3.4 Amount/quantity thresholds

**No amount or quantity thresholds are implemented in the frontend for button visibility or submit.** All submit/button logic is driven by status, stage, house rules, and selected hit status only.

---

## 4. Node Field Permission Matrix

**Role:** All nodes are **role-independent** in the current code (no role-based show/hide or enable/disable of fields). Permissions vary only by **(A) Node**, **(C) Document/source type**, and **(D) key fields** where noted. **(E) Amount/quantity thresholds:** Not implemented for field visibility or editability; evidence: no conditional on price/quantity for Form.Item hidden/disabled/required in Application or Org.

**Block-level convention:** Blocks are logical UI sections (基本信息, 客户信息, 平台信息, 联系人信息, 地区, 销售负责人, 规则/备注, 审核信息). For each block: **Visible** = fields rendered; **Editable** = not disabled/readOnly; **Required** = rules with required: true; **Read-only** = display-only (no input).

---

### 4.1 Create apply form (销售申请 / 服务申请 / 申请平台)

| Block | Visible fields | Editable | Required | Read-only | Condition (C = source type) |
|-------|----------------|----------|----------|-----------|-----------------------------|
| 平台信息 | platform, sourceType (hidden) | platform | platform | sourceType | — |
| 客户信息 | customerId (hidden), customerName, orgFullName | customerName, orgFullName | customerName, orgFullName | customerId | — |
| 联系人信息 | contactPerson, phone, email | all | all | — | — |
| 合同/销售 (FACTORY only) | contractNo | contractNo | contractNo when NOT enterpriseDirect | — | (C) createSourceType === 'FACTORY'; required = NOT enterpriseDirect. Evidence: Application/index.tsx ModalForm `createSourceType === 'FACTORY' &&` Form.Item contractNo `rules={enterpriseDirect ? [] : [{ required, ContractRule }]}`. |
| 服务信息 (SERVICE only) | price, quantity, reason | all | all | — | (C) createSourceType === 'SERVICE'. Evidence: `createSourceType === 'SERVICE' && ( ProFormMoney, ProFormDigit, reason )` — Application/index.tsx. |

Evidence: `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` — ModalForm (create) Form.Item names and rules; createSourceType, enterpriseDirect from props/state.

---

### 4.2 Enterprise Confirm modal (营企确认)

| Block | Visible fields | Editable | Required | Read-only | Condition |
|-------|----------------|----------|----------|-----------|-----------|
| 申请单号/规则命中 | applicationNo (display), rejectReason (Alert if present), plannerCustomerExists (Alert), 首次开通 Alert when SERVICE | — | — | applicationNo, rejectReason, alerts | (C) currentRow.sourceType drives SERVICE block below. |
| 平台信息 | platform | platform | platform | — | — |
| 客户信息 | customerId (hidden), customerName, orgFullName | customerName, orgFullName | all | customerId | — |
| 联系人信息 | contactPerson, phone, email | all | all | — | — |
| 销售负责人 | salesPerson | salesPerson | salesPerson | — | — |
| 备注与原因 (SERVICE) | price, quantity, reason | all | all | — | (C) currentRow.sourceType === 'SERVICE'. Evidence: `currentRow?.sourceType === 'SERVICE' ? ( price, quantity, reason ) : ( contractNo, ccPlanner when FACTORY )` — Application/index.tsx planner Form. |
| 合同/抄送 (FACTORY) | contractNo, ccPlanner | all | all | — | (C) currentRow.sourceType === 'FACTORY'. Evidence: same block. |

Evidence: `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` — Modal 营企确认, Form plannerForm, Form.Item names; currentRow.sourceType.

---

### 4.3 Duplicate-warning modal (重复提醒)

| Block | Visible fields | Editable | Required | Read-only | Condition |
|-------|----------------|----------|----------|-----------|-----------|
| 规则命中信息 | Hit cards: orgCodeShort, customerName, orgFullName, email, phone, 目前状态 (status label) | — | — | All (display + hit selection) | Hit list is clickable for selection; no form fields. Status = getPlatformStatusDisplay (已启用/已禁用/已欠费). Evidence: HitAnalysisPanel, DuplicateCheckModal — no Form. |

---

### 4.4 Admin audit modal (管理员审核)

| Block | Visible fields | Editable | Required | Read-only | Condition |
|-------|----------------|----------|----------|-----------|-----------|
| 审核信息 (left col) | applicationNo, platform, sourceType, customer, orgCodeShort, orgFullName, contactPerson, phone, email, contractNo (if set), price/quantity/reason (if set) | — | — | All (display; duplicate hits highlighted) | Data from currentRow; adminHitMatches for red highlight. Evidence: Application/index.tsx admin Modal Row/Col. |
| 审核信息 (form) | orgCodeShort, region | orgCodeShort, region | orgCodeShort, region | — | — |
| 操作 | 通过, 驳回 | — | — | — | — |

Evidence: `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` — adminForm, handleApprove (orgCodeShort, region required), REGION_OPTIONS Select.

---

### 4.5 Org create modal (新建机构)

| Block | Visible fields | Editable | Required | Read-only | Condition |
|-------|----------------|----------|----------|-----------|-----------|
| 平台信息 | platform | platform | platform | — | — |
| 客户信息 | customerId (hidden), customerName, orgFullName | customerName, orgFullName | customerId, customerName, orgFullName | customerId | — |
| 基本信息 | orgCodeShort (label/rules by platform codeValidatorType) | orgCodeShort | orgCodeShort | — | Platform config codeValidatorType: UPPERCASE / MIXED|CHINESE / other. Evidence: Org/index.tsx getConfig(createPlatform).codeValidatorType. |
| 联系人信息 | contactPerson, contactPhone, contactEmail | all | all | — | — |
| 地区 | region | region | — | — | Select REGION_OPTIONS. |
| 销售负责人 | salesPerson (申请人) | salesPerson | salesPerson | — | — |
| 备注与原因 | remark | remark | — | — | — |
| 启用状态 | isActive | isActive | — | — | Default true. |

Evidence: `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` — createForm, CustomerSelectField, REGION_OPTIONS, openCreateModal setFieldsValue({ isActive: true }).

---

### 4.6 Application list (申请管理 — 待处理 / 申请历史)

| Block | Visible fields | Editable | Required | Read-only | Condition |
|-------|----------------|----------|----------|-----------|-----------|
| Table columns | applicationNo, platform, sourceType, customerName, orgCodeShort, orgFullName, status, createdAt, 操作 | — | — | All (table read-only; 操作 = 详情/营企处理/管理员审核) | Row action visibility by row.status (see §1.2). Evidence: Application/index.tsx columns render. |

---

### 4.7 Placeholder pages (开卡申请 / 申请核查 / 申请复用)

| Node/State | Permissions |
|------------|-------------|
| 开卡申请 (sim-activation) | **N/A (placeholder).** No form; page shows title + “功能开发中，敬请期待。” Evidence: SimActivation.tsx PlaceholderApplication. |
| 申请核查 (verification) | **N/A (placeholder).** Same. Evidence: Verification.tsx. |
| 申请复用 (reuse) | **N/A (placeholder).** Same. Evidence: Reuse.tsx. |

---

## 5. Type Difference Matrix

Keyed by **单据类型/来源类型** as implemented in code. **委托类型 (delegation type):** Not implemented — no separate “delegation” dimension in frontend; no variable or type named delegation. Evidence: grep for delegation/委托 in Platform Application and Org yields no conditional logic.

| Type (label) | Internal key / condition | Buttons (visibility / outcomes) | Field permissions / required | Validation / house rules | State transitions / next-step |
|--------------|---------------------------|---------------------------------|------------------------------|---------------------------|-------------------------------|
| **Sales apply** | sourceType === 'FACTORY' && !enterpriseDirect | 提交, 保存草稿, 恢复草稿/清除草稿. Duplicate: 返回编辑, 确认新增 when !violatesHouseRules, 申请复用 when selectedHitCanReuse. | 平台信息 + 客户信息 + 联系人信息 + **合同号** (required). No price/quantity/reason. | platform, customerName, orgFullName, contactPerson, phone, email, **contractNo** required; ContractRule. House rules per platform config. | Submit → check-duplicates → no match: doCreate → success → close, clear draft; matches: open duplicate modal. 确认新增 → doCreate → close modals. |
| **Service apply** | sourceType === 'SERVICE' | Same as Sales except duplicate: **申请核查** when violatesHouseRules (service stage). | 平台信息 + 客户信息 + 联系人信息 + **单价, 台数, 原因** (required). No contractNo. | price, quantity, reason required. House rules same. | Same flow. 申请核查 → message.info (placeholder). |
| **Enterprise apply (营企申请)** | enterpriseDirect === true (FACTORY with enterprise flag) | Same as Sales; modal title “申请平台”. 开卡申请 in duplicate when enterprise_confirm + violatesHouseRules. | **合同号** not required (enterpriseDirect ? [] : [required, ContractRule]). salesPerson optional (no error when absent in create payload). | Same required as Sales minus contractNo. | onSuccess → navigate /platform/applications; onCancel → navigate /platform/orgs. |
| **Enterprise Confirm (营企确认)** | pendingCreateValues == null (duplicate opened from planner submit) | 关闭申请, 取消, 保存草稿, 提交至管理员. Duplicate: 返回编辑, 确认新增 when !violatesHouseRules, 申请复用 when selectedHitCanReuse, **开卡申请** when violatesHouseRules. | Fields driven by **currentRow.sourceType** (SERVICE: price, quantity, reason; FACTORY: contractNo, ccPlanner). All main fields required. | plannerForm validateFields; doPlannerSubmit. House rules same. | 提交至管理员 → check-duplicates → no match: doPlannerSubmit → close; matches: open duplicate. 确认新增 from duplicate → doPlannerSubmit → clear planner draft. |
| **Admin review (管理员审核)** | row.status === 'PENDING_ADMIN'; modal opened from list | 通过 (handleApprove), 驳回. No duplicate footer in this modal. | Left: read-only application summary. Right: HitAnalysisPanel (read-only). Form: orgCodeShort, region (required). | orgCodeShort, region required for 通过. | 通过 → POST approve → optional “是否办理SIM” confirm → reload. 驳回 → reject modal → POST reject → reload. |

**Condition expressions (AND/OR):**

- **确认新增 visible:** `(A) Node = 重复提醒 AND (D) !violatesHouseRules`
- **申请复用 visible:** `(A) Node = 重复提醒 AND (D) selectedHitCanReuse` (selected hit 已禁用 OR 已欠费)
- **开卡申请 visible:** `(A) Node = 重复提醒 AND (C) Enterprise Confirm (pendingCreateValues == null) AND (D) violatesHouseRules`
- **申请核查 visible:** `(A) Node = 重复提醒 AND (C) Service (pendingCreateValues?.sourceType === 'SERVICE') AND (D) violatesHouseRules`
- **合同号 required:** `(C) createSourceType === 'FACTORY' AND NOT enterpriseDirect`
- **price/quantity/reason block visible:** `(C) createSourceType === 'SERVICE'` (create form) OR `(C) currentRow.sourceType === 'SERVICE'` (营企确认)
- **营企处理 link visible:** `(D) row.status IN (PENDING_PLANNER, PENDING_CONFIRM, CLOSED)`
- **管理员审核 link visible:** `(D) row.status === 'PENDING_ADMIN'`

Evidence (summary): Application/index.tsx — createSourceType, enterpriseDirect, pendingCreateValues, currentRow.sourceType; renderFooter; Form.Item conditionals. Org/index.tsx — stage enterprise_direct only.

---

## 6. Appendix — Code evidence

| Item | File path | Symbol / location |
|------|-----------|--------------------|
| Duplicate modal stage type | `frontend/dfbs-ui/src/features/platform/components/DuplicateCheckModal/index.tsx` | `DuplicateModalStage`, `canApplyReuseForHit` |
| Duplicate footer logic (createModalOnly) | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | `renderFooter` (createModalOnly branch): isBlocked, showConfirmCreate, showApplyReuse, isService, isEnterpriseConfirm |
| Duplicate footer logic (main) | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | `renderFooter` (main return): same variables, buttons array |
| House rules violation | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | violationEmail, violationPhone, violationName (config.ruleUnique*, duplicateMatches, matchReason) |
| Org duplicate footer | `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` | `renderFooter` for DuplicateCheckModal, stage="enterprise_direct" |
| Application status partition | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | `APPLICATION_STATUS_PARTITION`, tableRequest statuses |
| List row actions | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | columns render: 详情, 营企处理 (PENDING_PLANNER \|\| PENDING_CONFIRM \|\| CLOSED), 管理员审核 (PENDING_ADMIN) |
| Platform config rules | `frontend/dfbs-ui/src/features/platform/services/platformConfig.ts` | `PlatformConfigItem`: ruleUniqueEmail, ruleUniquePhone, ruleUniqueOrgName, codeValidatorType |
| Platform status display | `frontend/dfbs-ui/src/features/platform/components/HitAnalysisPanel/index.tsx` | `getPlatformStatusDisplay`, `getPlatformStatusLabelForOrg` |
| Apply entry (sales/service/enterprise) | `frontend/dfbs-ui/src/pages/Platform/applications/Apply.tsx` | source param, createModalOnly, initialSourceType, enterpriseDirect |
| Routes (placeholders, apply) | `frontend/dfbs-ui/src/App.tsx` | platform/apply, platform/applications/reuse, verification, sim-activation |
| Placeholder components | `frontend/dfbs-ui/src/pages/Platform/applications/Reuse.tsx`, `Verification.tsx`, `SimActivation.tsx` | PlaceholderApplication title/subTitle |
| Draft keys | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | draftKey `platform-apply-${createSourceType}${enterpriseDirect ? '-enterprise' : ''}`, plannerDraftKey `platform-apply-enterprise-confirm` |
| Org create draft | `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` | `useDraftForm('platform-org-create-admin')` |
| No thresholds | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | No conditional on price/quantity for buttons; only status/stage/violatesHouseRules/selectedHitCanReuse |
| Create form fields (FACTORY/SERVICE/enterpriseDirect) | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | ModalForm Form.Item: platform, sourceType, CustomerFieldCreate, orgFullName, contactPerson, phone, email; createSourceType === 'FACTORY' && contractNo (enterpriseDirect ? [] : required); createSourceType === 'SERVICE' && price, quantity, reason |
| 营企确认 form fields by sourceType | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | plannerForm; currentRow?.sourceType === 'SERVICE' ? price/quantity/reason : contractNo + (FACTORY && ccPlanner) |
| Admin audit form | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | adminForm orgCodeShort, region; handleApprove validates orgCodeShort, region; REGION_OPTIONS |
| Org create form blocks | `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` | createForm: platform, CustomerSelectField, orgCodeShort (getConfig codeValidatorType), orgFullName, contactPerson, contactPhone, contactEmail, region (Select), salesPerson, remark, isActive |
| Delegation type not implemented | `frontend/dfbs-ui/src` (Platform Application, Org) | No symbol or conditional on “委托” or delegation; sourceType and enterpriseDirect only |

---

## 7. Coverage checklist (self-review)

| Item | Status | Notes |
|------|--------|-------|
| **All nodes covered** | ✓ | Create apply, 营企确认, 重复提醒, 管理员审核, 新建机构, 申请管理 list, 开卡申请/申请核查/申请复用 placeholders. |
| **All buttons covered** | ✓ | §1 Node–Button matrix: 返回编辑, 确认新增, 申请复用, 开卡申请, 申请核查; 详情, 营企处理, 管理员审核; 平台管理 toolbar; create/营企确认/新建机构 footer; placeholders. |
| **Field permissions covered** | ✓ | §4 Node Field Permission Matrix: block-level visible/editable/required/read-only per node; conditions by (C) source type where applicable. |
| **Type differences covered** | ✓ | §5 Type Difference Matrix: Sales, Service, Enterprise apply, Enterprise Confirm, Admin review; buttons, fields, validation, next-step; 委托类型 documented as not implemented. |
| **Threshold dimension (E)** | ✓ | Explicitly stated: no amount/quantity thresholds for button visibility or field permissions; evidence in §4 intro and §6 appendix. |

---

*End of Platform Application Flow rules report.*
