# DFBS_TPL_B.md
> Template library for **B / Planning Lead** only.
> This file defines **output shapes** for Planning Lead.
> It is **not** a policy file and does **not** override:
> - `PROJECT_COLLAB_CHARTER_v1.md`
> - `B_PROJECT_INSTRUCTION.md`
>
> Locate templates fast via `## TEMPLATE: <TID>` and Ctrl+F.

---

## TEMPLATE: TPL.PLANNING.SPLIT_STEPS.TO_DELIVERY

```text
[PLAN -> Delivery Manager]
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Title: <short title>

A) Frozen Boundary Carry-over (from Discovery; do not expand)
User-visible goal:
- ...

Trigger / Where:
- ...

Anchors (use confirmed anchors only; Unknown is acceptable; do NOT invent or force all four):
- Module: <... / Unknown>
- Route / Entry: <... / Unknown>
- Process node: <... / Unknown>
- Object scope: <... / Unknown>

Scope (only do these now):
- ...

Non-goals (explicitly NOT now):
- ...

Minimum “Looks Good” (UI-level acceptance anchor):
- ...

Wheel decision:
- <Yes / No / Maybe>

B) Planning Basis (facts only; no guessing)
From Discovery freeze:
- ...

From handover / evidence / user artifacts:
- ...

Unknown / not confirmed:
- ...

C) Steps (product-level, user-visible goals)
- <AUTO_KEY-YYMMDD-NNN-01>: <one-line user-visible goal>
- <AUTO_KEY-YYMMDD-NNN-02>: <one-line user-visible goal>
- <AUTO_KEY-YYMMDD-NNN-03>: <one-line user-visible goal>
- ...

D) Dependencies / Notes
- <...-02> depends on <...-01> because <product reason>
- <...-03> depends on <...-02> because <product reason>
- Unknown: <what is unknown and why it matters>

E) Hard Stop
- Stop after: <step id / none>
- Why: <product reason; e.g. base layer must be checked before first downstream consumer continues>

F) Risk (product-logic only; not engineering)
- Potential conflict with: <other Parent IDs if known>
- Potential missing precondition: <entry / status / permission / object> (Unknown if not confirmed)
- Freeze prerequisite insufficient?: <Yes / No> — <why>

G) Backlog / Todo (full, titles ok but not omitted)
- <item 1>
- <item 2>
- ...

H) Planning Notes (product-level only)
- <1–3 bullets max>
[/PLAN -> Delivery Manager]
````

---

## TEMPLATE: TPL.PLANNING.DEPENDENCY.NOTE

```text
[DEPENDENCY NOTE]
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Related Step(s): <...-01 / ...-02 / ...>

Dependency type: <Product precondition / Flow ordering / Permission / Status definition / Unknown>

What depends on what:
- <A> -> <B>

Why it matters (plain words):
- ...

Current evidence (facts only):
- From freeze / user artifact / handover / evidence-only: ...
- Unknown / not confirmed: ...

Next action (one):
- Ask user for artifact: <what>
- Or send Clarify Query to Discovery Lead: <Query ID>
[/DEPENDENCY NOTE]
```

---

## TEMPLATE: TPL.PLANNING.SNAPSHOT.FULL

```text
[PLANNING SNAPSHOT — FULL]
As-of: <YYYY-MM-DD>
Owner: Planning Lead

A) Active Parent Frozen IDs (from Discovery)
- <AUTO_KEY-YYMMDD-NNN> — <title> — Status: <Planning / Blocked / Ready for Delivery / Handed to Delivery / Awaiting UI Check / Verified by User>

B) Step Plan (full)
Parent: <AUTO_KEY-YYMMDD-NNN> — <title>

Frozen boundary carry-over:
- Trigger / Where: ...
- Scope: ...
- Non-goals: ...
- Minimum “Looks Good”: ...
- Wheel: <Yes / No / Maybe>

Facts basis:
- From Discovery freeze: ...
- From handover / evidence / artifacts: ...
- Unconfirmed assumptions: ...

Steps:
- <...-01>: <goal> — Status: <Ready / Blocked / Handed to Delivery / Awaiting UI Check / Verified by User> — Blocked by: <... or None>
- <...-02>: <goal> — Status: ...
- <...-03>: <goal> — Status: ...

Notes:
- Dependencies: ...
- Hard stop: <step id / none> — Why: ...
- Unknowns: ...

(Repeat the Parent block above for each Parent Frozen ID.)

C) Backlog / Todo (full, not omitted)
- <backlog item 1> (link to Parent ID if any)
- <backlog item 2>
- ...

D) Cross-item Conflicts / Risks (product-logic only)
- <risk 1>
- <risk 2>

E) Clarification Queries (if any)
- <ParentID-Q01>: <one-line question summary> — Status: <Sent / Replied / Integrated>
[/PLANNING SNAPSHOT — FULL]
```

---

## TEMPLATE: TPL.PLANNING.CLARIFY.TO_DISCOVERY

```text
[PLANNING CLARIFY -> Discovery Lead]
Query ID: <ParentFrozenID-QNN>    // e.g. PASTE-260210-112-Q01
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Title: <short>

Clarification type:
- <Freeze interpretation / Missing fact within frozen scope / Possible boundary change>

Why this blocks planning (1 line):
- ...

Questions (independent; max 1–5):
- Q1: ...
- Q2: ...
- Q3: ...

Boundary impact:
- Re-freeze required? <Yes / No / Maybe>
- Why: ...

What I will do after your reply:
- Update dependencies / order / stop point and refresh the planning output.
[/PLANNING CLARIFY -> Discovery Lead]
```
