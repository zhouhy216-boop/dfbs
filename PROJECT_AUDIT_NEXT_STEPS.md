# Recommended Next Steps

**Basis**: Gaps from PROJECT_AUDIT_MODULE_CAPABILITIES.md and PROJECT_AUDIT_BASELINE_COVERAGE.md (vs final_01).  
**Scope**: Backend only; priority = unblock B1 or close high-impact PARTIAL/TODO.

---

1. **Data Correction / 数据更正单（Correction）**
    * **Reason**: B1 explicitly lists “数据更正单（Correction）” as V1 做; currently **TODO** (no entity or API). Implementing a minimal Correction entity + CRUD + audit trail closes the only B1 TODO and aligns with baseline.

2. **RMA / 退货·返修·换货 clarity**
    * **Reason**: B1 lists “RMA（退货/返修/换货）”; backend has **返修** (repair_record) but no 退货 or 换货 flow. Either (a) define 退货/换货 as separate entities or state flows and add minimal APIs, or (b) document “RMA = repair only for V1” in baseline and mark PARTIAL as accepted. This removes ambiguity for QA and future scope.

3. **经办人事实统计页 API（非绩效考核）**
    * **Reason**: B1 lists “经办人事实统计页” as V1 做; currently **PARTIAL** (ExpenseStats exists but no dedicated “经办人事实” API). Add a single read-only API (e.g. by operator/period/summary) so the front end can build the page without inventing logic; keeps backend as single source of truth.

---

**Optional follow-ups** (after the above):

* **Rule engine / 规则引擎 (1.2)**: Start with a small scope (e.g. “mandatory attachment by target+point” already in AttachmentRuleService); extend to one more use case (e.g. visibility or workflow condition) and add tests.
* **Config center (1.1)** or **Task center (1.6)**: Defer until product prioritizes platform底座 over new business features; document as “留口” if needed.
