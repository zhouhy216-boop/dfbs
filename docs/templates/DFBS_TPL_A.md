# DFBS_TPL_A.md
> Template library for **A / Discovery Lead** only.
> This file defines **output shapes** for Discovery Lead.
> It is **not** a policy file and does **not** override:
> - `PROJECT_COLLAB_CHARTER_v1.md`
> - `A_PROJECT_INSTRUCTION.md`
>
> Locate templates fast via `## TEMPLATE: <TID>` and Ctrl+F.

---

## TEMPLATE: TPL.DISCOVERY.FREEZE.TO_PLANNING

```text
[DISCOVERY FREEZE -> Planning Lead]
ID: <AUTO_KEY-YYMMDD-NNN>
Title: <short>

User-visible goal (plain words):
- <what the user should be able to see / do / feel after this is delivered>

Trigger / Where:
- <where it starts / where it happens / entry / route family / page family if known>

Anchors (use confirmed anchors only; Unknown is acceptable; do NOT invent or force all four):
- Module: <... / Unknown>
- Route / Entry: <... / Unknown>
- Process node: <... / Unknown>
- Object scope: <... / Unknown>

Scope (do now):
- <item 1>
- <item 2>
- <item 3>

Non-goals (explicitly NOT now):
- <item 1>
- <item 2>
- <item 3>

Unknowns (do not guess):
- <item 1>
- <item 2>

Wheel check:
- Might be a reusable wheel? <Yes / No> (user decided)

Minimum “Looks Good” (how the user will judge in UI):
- <visible checkpoint 1>
- <visible checkpoint 2>
- <visible checkpoint 3>

Fact risk note (optional; only if the freeze can still remain valid without resolving it now):
- <unresolved fact risk / artifact still needed / route or repo fact to confirm later>
- <or write: none>

Notes for planning (product-level dependency hints only; no engineering implementation details):
- <dependency hint 1>
- <dependency hint 2>
[/DISCOVERY FREEZE -> Planning Lead]
````

---

## TEMPLATE: TPL.DISCOVERY.EVIDENCE_ONLY.TO_EXECUTOR

```text
[EVIDENCE ONLY -> Execution Engineer]
Request ID: <AUTO_KEY-YYMMDD-NNN-EVID>
Purpose: confirm real current project facts before discovery freeze

Why this fact is needed:
- <why the fact matters to discovery / why guessing would be dangerous>

Which decision is blocked until this fact is confirmed:
- <freeze / reuse / scope / route / object / ownership / current capability judgment>

Need evidence (exact items):
- Item 1: <what to verify>
- Item 2: <what to verify>
- Item 3: <what to verify>

How to collect (read-only / facts only):
- Commands to run (if any): <...>
- Files / modules / routes to inspect: <...>
- UI behavior to verify (if any): <...>

Output requirements:
- Short receipt (<=20 lines):
  - Completed? (Yes/No)
  - Findings: <fact + pointer>
  - Not found items (if any): <list>
  - Build/test status only if relevant to the fact
  - 1 blocker question max (only if truly blocking)

Hard rules:
- Facts only. No planning. No new requirements.
- No code / behavior changes.
- If something cannot be confirmed, write exactly: Not verified
[/EVIDENCE ONLY -> Execution Engineer]
```

---

## TEMPLATE: TPL.DISCOVERY.ARCHIVE.INDEX_ADD_LINE

```text
| <ID> | <Title> | <Current status / phase> | <Latest clear conclusion or handoff note> | <Thread file path> |
```

---

## TEMPLATE: TPL.DISCOVERY.ARCHIVE.THREAD_FILE

```text
# <ID> <Title>

## Status
- Current phase: <...>
- Why archived now: <...>

## What is already clear
- <point 1>
- <point 2>
- <point 3>

## Still unclear / pending
- <point 1>
- <point 2>

## Latest agreed boundary (if any)
- User-visible goal: <...>
- Trigger / where: <...>
- Scope: <...>
- Non-goals: <...>
- Wheel decision: <Yes / No / Not discussed>

## Important references
- Product anchors: <module / route / process / object if known>
- Key artifacts: <screenshots / docs / files if any>

## Next recommended resume point
- <where to restart next time without redoing the whole discussion>
```

---

## TEMPLATE: TPL.DISCOVERY.REPLY_TO_PLANNING_CLARIFY

```text
[DISCOVERY REPLY -> Planning Lead]
Query ID: <ParentFrozenID-QNN>
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>

A) Direct Answers
- A1: <answer>
- A2: <answer>
- A3: <answer>

B) Clarified Freeze Facts (only if needed; no scope expansion)
- Trigger / Where:
  - <...>
- Scope:
  - <...>
- Non-goals:
  - <...>
- Minimum “Looks Good”:
  - <...>
- Unknowns:
  - <...>

C) Out of current freeze scope
- <item 1>
- <item 2>
- <or write: none>

D) Fact risk note before any stronger conclusion (if any)
- <artifact / screenshot / sample / evidence-only need>
- <or write: none>

E) Planning note (1–3 lines max)
- <how B should interpret this clarification without expanding boundary>
[/DISCOVERY REPLY -> Planning Lead]
```

```

