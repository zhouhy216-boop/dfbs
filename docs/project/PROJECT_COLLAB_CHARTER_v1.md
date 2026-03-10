# PROJECT_COLLAB_CHARTER_v1

## 0. Purpose
This file is the project’s **shared collaboration charter**.

It defines the shared rules that all participants must follow:
- source layering
- fact priority
- shared anchors
- handoff interfaces
- stop lines
- rollback / reopen routing
- source maintenance ownership

It does **not** replace the role-specific rules of A / B / C / Cursor, and it does **not** describe each role’s detailed working method.

---

## 1. Role Boundaries (interfaces only, not detailed methods)
- User: final decision-maker; decides boundary, priority, acceptance, and whether a long-lived source should be updated.
- A: turns user intent into a clear boundary and freezes it for B.
- B: turns frozen scope into product steps, order, dependencies, Unknowns, risks, conflicts, and stop points.
- C: turns steps into execution tickets and manages the verify / fix loop.
- Cursor: executes tickets, gathers evidence, and returns receipts.
- **No role may replace another role’s decision authority.**

---

## 2. Shared Project Source Model
> Purpose: every role should know what kinds of sources exist, what each one is for, and when to read it first.

### 2.1 Long-lived project sources (stable, lower-frequency updates)
These sources establish shared language, rules, interfaces, and reusable structure.

#### A. Shared collaboration charter
| Source | Description | When to read first |
|---|---|---|
| `PROJECT_COLLAB_CHARTER_v1.md` | Shared collaboration charter; defines source layering, fact priority, interfaces, stop lines, rollback routing, and source-maintenance rules | Read first when entering the project; revisit when rule conflicts appear |

#### B. Product alignment pack
| Source | Description | When to read first |
|---|---|---|
| `MODULE_ROUTE_ANCHORS_v0.1.md` | Module ↔ route / entry anchor table | Read first when discussing which existing page / entry a module belongs to |
| `BUSINESS_MAP_v0.1.md` | Business module map, role relationships, module-level structure | Read first when discussing which business capability is involved |
| `PROCESS_MAP_v0.1.md` | Mainline processes, side flows, and mid-node entry patterns | Read first when discussing where a change lands in the process |
| `OBJECT_MAP_v0.1.md` | Business objects and their relationships | Read first when discussing which objects are affected and how they relate |

#### C. Role-specific template libraries
| Source | Description | Who uses it / when to read first |
|---|---|---|
| `DFBS_TPL_A.md` | Template library for A (Discovery Lead) | Used by A when freezing, archiving, and replying to clarification |
| `DFBS_TPL_B.md` | Template library for B (Planning Lead) | Used by B when splitting steps, outputting plan snapshots, and raising clarification |
| `DFBS_TPL_C.md` | Template library for C (Delivery Manager) | Used by C when issuing execution tickets, fix tickets, evidence-only tickets, impact checks, and acceptance steps |

#### D. Role-specific full rule files
> These are long-lived sources, but their full detailed contents are **not** meant to be fully shared across all roles.  
> Every role only needs to fully read its own detailed rule file.

| Source | Description | Who reads it |
|---|---|---|
| `A_PROJECT_INSTRUCTION.md` | A’s full detailed rule file | A only |
| `B_PROJECT_INSTRUCTION.md` | B’s full detailed rule file | B only |
| `C_PROJECT_INSTRUCTION.md` | C’s full detailed rule file | C only |
| Cursor rule source | Cursor’s role-specific rule source | Cursor only |

#### E. Optional module-specific normalized sources
Module-specific normalized sources may be created when a module needs a stable, reusable long-lived description beyond raw user materials.

Purpose:
- turn raw business materials into a stable, structured, GPT-friendly and Cursor-friendly source
- reduce repeated interpretation from screenshots, spreadsheets, copied notes, and loose natural language
- provide a consistent module-level source for discovery, planning, delivery, and execution

Rules:
- If a normalized module source exists, it becomes the preferred long-lived source for that module.
- Raw materials remain as evidence / reference, but should not remain the default primary wording once a normalized source is accepted.
- A normalized module source should be updated only when the module’s real wording, scope, or structure changes.

Recommended content structure:
- Module name
- Purpose / user-visible goal
- Entry / route anchor(s)
- Roles involved
- Main object(s)
- Main flow
- States
- Actions
- Ownership / visibility semantics (if relevant)
- Audit / history requirements
- Related modules / dependencies
- v1 scope
- Non-goals
- Unknowns
- Source references

When to read first:
- when discussing a specific module in depth
- when freezing module boundary
- when planning module steps
- when delivering or executing that module

---

### 2.2 Stage snapshots (updated over time)
These sources describe the **current real system state**. They are stage snapshots, not long-lived rules.

Examples include:
- handover fact pack
- current-stage repo evidence
- current-stage evidence-only receipts
- current-round user-provided checkable artifacts

Typical use:
- judging what the system really can do now
- confirming whether a page, entry, table, field, or capability already exists
- confirming reuse status
- confirming current limitations or incomplete areas

---

### 2.3 Non-project sources (must NOT be used as default authority)
The following are **not** project sources by default and must not be used directly as authority:
- old freeze handoffs
- old split maps
- old execution tickets
- old chat conclusions
- intermediate solutions already proven misaligned
- any old material not explicitly confirmed by the user and not included in the current project source set

---

## 3. Source Layering Principle
### 3.1 Long-lived sources
Long-lived sources define:
- shared language
- rules
- templates
- stable interfaces
- module-level normalized wording (when available)

### 3.2 Stage snapshots
Stage snapshots define:
- the current real repo state
- which capabilities already exist, which are incomplete, and which are absent

### 3.3 One critical distinction
- **Product alignment pack** defines discussion anchors and product intent
- **Stage snapshots / evidence** define current real system state

Both matter, but they serve different purposes and must not be mixed.

---

## 4. Fact Priority
When discussing or making decisions, use this priority order:

1. current repo reality  
2. current-stage handover fact pack / evidence-only results  
3. product alignment pack  
4. user-confirmed wording in the current round  

Notes:
- No role may treat a guessed system state as fact.
- If stage facts and the product alignment pack conflict, do **not** auto-complete or guess. Explicitly mark:
  - conflict
  - gap
  - not confirmed

---

## 5. Source Maintenance and Ownership
### 5.1 Shared charter and alignment pack
Update the shared charter or product alignment pack only when one of these truly changes:
- shared collaboration rule
- shared interface boundary
- module boundary
- route anchor
- process anchor
- object relationship

They should not be changed during ordinary requirement discussion unless the shared baseline itself has clearly changed.

### 5.2 Handover fact pack
Update handover only in these cases:
- stage transition
- critical fact correction
- user explicitly requests a baseline rebuild

Handover is not a real-time log and should not be rewritten after every normal requirement discussion.

### 5.3 Role-specific rules and templates
Role-specific rules and template libraries are long-lived sources and should not be modified frequently during ordinary requirement discussion.

### 5.4 Module-specific normalized sources
Create or update a normalized module source only when that module’s real business wording, structure, or rule basis changes.

### 5.5 Who may propose and who decides
- Any role may propose that a long-lived source needs updating if repeated ambiguity, repeated mismatch, or repeated rework is happening.
- A and B may propose business-side normalization or clarification needs.
- C may propose update needs when delivery repeatedly hits the same unclear business baseline.
- Cursor may draft evidence-backed structure or wording, but may **not** decide business meaning by itself.
- The **user** decides whether a proposed long-lived source update is accepted.

Once a long-lived source update is accepted by the user, it supersedes older informal wording.

---

## 6. Shared Discussion Anchors
All discussions, freezes, step splits, and tickets should be anchored, as much as possible, to these four dimensions:
- module
- route / entry
- process node
- object scope

Requirements:
- do not say only “that page”
- do not say only “that process”
- do not say only “something like that”
- every change should ideally answer:
  - which module it belongs to
  - which route / entry it maps to
  - where it lands in the process
  - which objects it affects

If one anchor is genuinely unknown, mark it Unknown.  
Do not invent fake anchors to make something look complete.

---

## 7. Handoff Interfaces
### 7.1 A → B
- A hands off only the frozen boundary.
- A’s freeze does not automatically mean “system reality has already been fact-verified”.
- If the freeze depends on current capability assumptions, A should ground it in facts; otherwise B may mark it as “freeze premise insufficient”.

### 7.2 B’s responsibility
B may:
- organize product steps and order
- surface dependencies, Unknowns, risks, and conflicts
- stop and push back for clarification when frozen boundary and fact baseline do not align
- decide whether a strong stop is needed between an enabling step and its first downstream consumer

B may not:
- fill in unfrozen meanings by itself
- turn Unknowns into assumed scope
- turn risk notes into product requirements
- replace A’s frozen boundary with its own interpretation

### 7.3 B ↔ User
The user may directly clarify **small planning questions inside the frozen boundary**, such as:
- terminology
- dependency order
- small-scope trade-offs
- concrete values for Unknowns
- whether a step should stop first
- whether a stop point should be explicit

But if the clarification would change:
- user goal
- scope
- non-goals
- success criteria
- mainline direction

then it must go back to A.

### 7.4 B → C
- B hands off product steps, dependencies, Unknowns, risks, and execution gate conditions.
- B must not hand off new unconfirmed scope.
- If B writes a strong stop, C must not auto-continue past that stop.

### 7.5 C → Cursor
- C issues only execution tickets, fix tickets, evidence-only tickets, and impact-check tickets.
- Cursor must not replace A or B in making product decisions.

### 7.6 Acceptance
- Default user acceptance is **UI-only verification**
- The user must not be required to perform API / DB / status-code / migration-level acceptance

---

## 8. Rollback / Reopen Routing
If a later finding invalidates an earlier assumption, the problem must be routed back to the correct layer instead of being silently absorbed by the current role.

### 8.1 Stay in C
Keep it in C if:
- the implementation is wrong
- the current ticket failed
- the fix is still inside the already-agreed boundary and planning

### 8.2 Go back to B
Go back to B if:
- step order is wrong
- dependency logic is wrong
- a stop point / gate is wrong
- the planning shape no longer fits the frozen boundary even though the boundary itself still stands

### 8.3 Go back to A
Go back to A if:
- the user-visible goal is wrong
- the boundary is wrong
- scope / non-goals changed
- the meaning of the requirement changed
- a new semantic is needed outside the current freeze

No role may silently handle an upstream-layer problem as if it were only a downstream execution problem.

---

## 9. Overlap / Replacement Rule
When a new parent item explicitly replaces, redoes, or narrows part of an older one:

- B does **not** need to judge the quality of the old execution
- B only needs to recognize whether the new item:
  - overlaps the old boundary
  - replaces part of the old boundary
  - changes the planning assumptions of a still-open item

If that overlap changes the actual product boundary, it must go back to A.  
If the new boundary is already explicitly frozen, B may plan from the new valid boundary and mark the old one as superseded in planning notes when needed.

---

## 10. Stop Lines / Prohibited Behavior
All roles must obey the following:

1. Without factual basis, no one may judge current system reality.  
2. Do not treat a “possibly existing capability” as a “directly reusable capability”.  
3. Do not treat an incomplete capability as a completed one before confirmation.  
4. Do not automatically expand a freeze handoff into a mainline strategy or foundation-layer solution.  
5. Do not inject unconfirmed meanings into steps, tickets, or acceptance.  
6. Do not push technical acceptance work onto the user.  
7. When facts are insufficient, prefer evidence gathering over guesswork.  
8. Do not silently skip a strong stop written by planning.  
9. Do not silently convert a rollback-to-A or rollback-to-B problem into a local fix-only problem.  

---

## 11. Rule Sharing Principle
To avoid role confusion, collaboration rules are split into two layers:

### 11.1 Shared across all roles
- this charter
- product alignment pack
- stage fact sources
- knowledge that template files exist and what they are for

### 11.2 Not fully shared
- the full detailed role-specific rules of A / B / C / Cursor

Each role should fully read only its own detailed rules.  
Other roles only need the interface boundary, not the entire detailed method.

---

## 12. Version and Effectiveness
- This file version: v1
- When a new version is published, the previous version is automatically superseded
- All future collaboration defaults to the latest version
- If any role-specific rule conflicts with this charter, the **latest user-confirmed charter** takes precedence, and the role-specific rule should be updated as soon as possible