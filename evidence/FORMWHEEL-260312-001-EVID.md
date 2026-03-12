# FORMWHEEL-260312-001-EVID — Platform Application form reality

## Completed?
Yes

## Findings (fact + pointer)

**1. How the form is built**
- **Page/component**: Single page `src/pages/Platform/Application/index.tsx` (~1200 lines). One main file; no separate form subpages.
- **Pattern**: Mixed. Main UI = Tabs (待处理 = UnifiedProTable, 申请历史 = ApplicationsHistory). Forms = multiple modals: (a) **Create** = `ModalForm` (Pro) with vertical layout; (b) **Planner confirm** = antd `Modal` + `Form` (antd); (c) **Admin audit** = antd `Modal` with left read-only summary + right `HitAnalysisPanel` + center `Form` (orgCodeShort, region, actions); (d) **Reject** = antd `Modal` + `Input.TextArea` for reason; (e) **Detail** = antd `Modal`, read-only `<p>` list. Not a step form. No single shared form wrapper—each modal has its own Form/ModalForm.
- **Composition**: Page-local. Create reuses `CustomerFieldCreate`, `PlannerCustomerSelect`, `PlannerContractSelect` (inline components in same file using shared SmartReferenceSelect/SmartInput). No shared “form container” component.

**2. Reusable-capability breakdown**

| Capability | Location / fact | Classification |
|------------|----------------|-----------------|
| Form container / layout | ModalForm (Create), Modal+Form (Planner, Admin). layout="vertical". No shared wrapper. | Reusable with small patch (use same primitives elsewhere) |
| Grouped sections | Inline Alert + Form.Item blocks; no section component. | Not present |
| Field rendering | Form.Item + Input/Select/ProFormMoney/ProFormDigit; page-local wrappers (CustomerFieldCreate etc.) | Reusable as-is (antd/Pro + shared inputs) |
| Field validation | Form.Item rules; PhoneRule/EmailRule from `shared/utils/validators/common`; ContractRule/OrgCodeRule/OrgCodeUppercaseRule from `features/platform/utils/validators` | Reusable as-is (shared rules); platform rules reusable with small patch if moved |
| Smart reference selection | `shared/components/SmartReferenceSelect` (entityType CUSTOMER/CONTRACT, value, onChange). Used in Create + Planner. | Reusable as-is |
| Smart text input / cleanup | `shared/components/SmartInput` (trim, noSpaces, uppercase, onlyLetters). Used for contactPerson, phone, email, contractNo, orgCodeShort. | Reusable as-is |
| Draft / save | `shared/hooks/useDraftForm(draftKey)`: saveDraft, loadDraft, clearDraft, hasDraft. Used for Create and Planner; Alert + 恢复草稿/清除草稿. | Reusable as-is |
| Duplicate-check flow | `features/platform/components/DuplicateCheckModal` + `HitAnalysisPanel`. Stage/violatesHouseRules/renderFooter/platform config–driven. | Exists but platform-specific; reusable with small patch if generalized (generic duplicate modal + configurable footer) |
| Submit / planner / admin | handleCreate, handlePlannerConfirm, handleApprove, handleRejectConfirm in page; permission checks + API. | Page-local; not generic form wheel |

**3. Presence of specific behaviors**
- **Draft save**: Yes. useDraftForm for Create (draftKey by sourceType/enterpriseDirect) and Planner (separate key).
- **Whole-form readonly/editable switching**: No generic toggle. Admin modal = read-only summary + editable Form (orgCodeShort, region only). Detail modal = fully read-only.
- **Node/state-specific field differences**: Yes. sourceType FACTORY vs SERVICE toggles fields (contractNo vs price/quantity/reason); enterpriseDirect; Planner shows salesPerson, ccPlanner conditionally; admin form orgCodeShort rules depend on platform config (codeValidatorType).
- **Field/group-level edit control**: No generic mechanism; conditional JSX by state.
- **Action-reason/remark capture**: Reject modal: TextArea for 驳回理由, state `rejectReason`. No shared “action-reason” component.
- **Audit/history display**: Detail modal shows current row only. No dedicated audit/history component. Not verified: backend history API.
- **Form-instance concept**: No. createFormRef, plannerForm, adminForm are page-local; no config-driven or single form-instance abstraction.

**4. Exact files carrying main form behavior**
- **Page**: `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx`
- **Shared**: `shared/hooks/useDraftForm.ts`, `shared/components/SmartInput/index.tsx`, `shared/components/SmartReferenceSelect/index.tsx`, `shared/utils/validators/common.ts` (PhoneRule, EmailRule)
- **Platform feature**: `features/platform/components/DuplicateCheckModal/index.tsx`, `features/platform/components/HitAnalysisPanel/index.tsx`, `features/platform/utils/validators.ts`, `features/platform/services/platformConfig.ts`

**5. Clearly platform-business-specific (do NOT treat as minimal generic form wheel)**
- DuplicateCheckModal: stage (sales/enterprise_confirm/service), violatesHouseRules, getConfig, ruleUniqueEmail/Phone/OrgName, platform-specific footer (开卡申请, 申请核查, 申请复用).
- HitAnalysisPanel: platform org status, platform-orgs API, DuplicateMatchItem.
- platformConfig service and codeValidatorType-driven orgCodeShort rules.
- Platform validators: OrgCodeRule, OrgCodeUppercaseRule (ContractRule could live in shared if needed).
- ApplicationRow, STATUS_MAP, REGION_OPTIONS, SALES_PERSON_OPTIONS, Create/Planner/Admin API paths and flow.

**6. Smallest reusable form-carrying slice already present**
- **Reusable as-is today**: `useDraftForm` + `SmartInput` + `SmartReferenceSelect` + `PhoneRule`/`EmailRule`. Optional: `ContractRule` (move from platform to shared if Contract Review needs it).
- **Reusable with small patch**: DuplicateCheckModal/HitAnalysisPanel only if generalized (e.g. generic “duplicate warning” modal + configurable footer and no platform config dependency). Contract Review V1 may not need duplicate-check in the minimal wheel.

## Not verified
- Backend audit/history API for applications.
- Whether Contract Review V1 will need duplicate-check at all (if not, duplicate flow is out of minimal wheel scope).

## Build/test status
N/A (read-only evidence).

## Blocker question
None.
