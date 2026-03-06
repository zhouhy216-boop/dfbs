# DICT-260303-001-PQ01 — Delivery Clarify (Type D multi-level cascades)

[DELIVERY CLARIFY -> Planning Lead]

**Query ID:** DICT-260303-001-PQ01  
**Parent Frozen ID:** DICT-260303-001  
**Related Step(s):** DICT-260303-001-03 / DICT-260303-001-05  
**Title:** Update Step-03 per Discovery freeze — Type D must support multi-level cascades (>=3 levels)

---

**Why execution is blocked (1 line):**  
Discovery updated V1 requirements: Type D is multi-level (>=3 levels), superseding the previously delivered 1-level-only Step-03; need an updated Step-03 plan + acceptance.

---

**Questions (planning-level only; independent; max 1–5):**

- **Q1:** Confirm we should REOPEN Step DICT-260303-001-03 (same Step ID) for a delta upgrade to multi-level cascades (tree), rather than creating a new step ID.

- **Q2:** Define the minimum acceptance for multi-level: at least 3 levels (e.g., province→city→district). Is there any practical cap you want documented (e.g., “supports N levels; safe cap may apply”)?

- **Q3:** Admin UX expectation for multi-level parent selection: should parent picker allow selecting ANY existing node as parent (to build arbitrary depth), or restrict to selecting only within the same dictionary and allow any depth (no “level types”)? Provide the minimal expected flows.

- **Q4:** Read contract: can business use the existing `parentValue` (direct-children) query repeatedly + client-side root filtering, or do you want an additive param like `rootsOnly` for convenience? (Note: current default “no parentValue = return ALL (roots+children)” must remain for backward compatibility.)

- **Q5:** Record decision for DICT-260211-001 relationship: if overlapping scope, close/merge it under DICT-260303-001; otherwise keep both as separate parents.

---

**What I will do after your reply:**  
Update split map, run impact check, then issue delta execution tickets to implement multi-level cascades with regression watchlist.

[/DELIVERY CLARIFY -> Planning Lead]
