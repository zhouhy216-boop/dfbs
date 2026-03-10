# DFBS_TPL_C.md
> Template library for **C / Delivery Manager** only.
> This file defines **output shapes** for Delivery Manager.
> It is **not** a policy file and does **not** override:
> - `PROJECT_COLLAB_CHARTER_v1.md`
> - `C_PROJECT_INSTRUCTION.md`
>
> Locate templates fast via `## TEMPLATE: <TID>` and Ctrl+F.

---

## TEMPLATE: TPL.DELIVERY.TICKET.TO_EXECUTOR

```text
[EXECUTION TICKET -> Execution Engineer]
Ticket ID: <AUTO_KEY-YYMMDD-NNN-STEP.SUB>     // e.g. PASTE-260210-112-01.a
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Parent Step ID: <AUTO_KEY-YYMMDD-NNN-01>
Title: <short>

A) ALIGNMENT ANCHORS (confirmed anchors only; Unknown is acceptable; do not invent)
- Business module: <... / Unknown>
- Route / Entry: <... / Unknown>
- Process node: <... / Unknown>
- Object scope: <... / Unknown>
- Anchor gap (if any): <none / describe>

B) CURRENT GOAL (user-visible)
- <plain user-visible outcome for this ticket only>

C) UI ACCEPTANCE HOOK (user must be able to verify in UI; no technical steps)
- UI entry (menu path + route / entry): ...
- UI actions (buttons / tabs / modals): ...
- Expected visible outcomes (labels / rows / toast / banner / modal state / etc.): ...
- Negative checks (must NOT happen): ...

D) SCOPE (do now)
- ...
- ...

E) NON-GOALS (explicitly NOT now)
- ...
- ...

F) DEPENDENCIES / UNKNOWNS
- Depends on: <... / none>
- Unknowns (do not guess): <... / none>

G) CONSTRAINTS
- Must: keep WIP=1 (this ticket only)
- Must: keep output short (see OUTPUT REQUIREMENTS)
- Must: stay inside current planned scope
- Must: if backend changes are required, ensure there is a minimal UI acceptance hook or explicitly say it is not yet UI-verifiable
- Never: implement anything outside Scope
- Never: change unrelated behavior
- Never: require the user to run API / DB / migration / console verification

H) IMPLEMENTATION NOTES (engineering-facing, still concise)
- <where to change / expected approach / relevant files / areas if known>
- If uncertain, ask 1 blocker question only.

I) EXECUTION GATE
- Continue allowed after PASS: <Yes / No>
- If No: wait for explicit planning confirmation or next handoff before later tickets.

J) OUTPUT REQUIREMENTS (keep it short)
- Completed? (Yes/No)
- Build/test status (only what is relevant to this ticket): <command + PASS/FAIL>
- Changed areas (very short: pages / modules / files)
- What to verify (1–3 bullets; UI-oriented if possible)
- 1 blocker question max (only if truly blocking)
- Appendix only if FAIL or explicitly requested

[/EXECUTION TICKET -> Execution Engineer]
````

---

## TEMPLATE: TPL.DELIVERY.BUGFIX.TICKET.TO_EXECUTOR

```text
[BUGFIX TICKET -> Execution Engineer]
Ticket ID: <AUTO_KEY-YYMMDD-NNN-STEP.SUB-fixN>   // e.g. PASTE-260210-112-01.a-fix1
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Parent Step ID: <AUTO_KEY-YYMMDD-NNN-01>
Related Exec Ticket: <AUTO_KEY-YYMMDD-NNN-01.a>
Title: <short>

A) ALIGNMENT ANCHORS (confirmed anchors only; Unknown is acceptable; do not invent)
- Business module: <... / Unknown>
- Route / Entry: <... / Unknown>
- Process node: <... / Unknown>
- Object scope: <... / Unknown>
- Anchor gap (if any): <none / describe>

B) BUG (observed)
- User observed: ...
- Expected: ...
- Actual: ...

C) UI ACCEPTANCE HOOK (UI-only)
- UI entry (menu path + route / entry): ...
- Steps to reproduce (UI): ...
- Expected visible result after fix: ...
- Negative checks (if any): ...

D) SCOPE (fix only)
- Fix the described behavior to match Expected.
- Keep the original ticket goal unchanged.

E) NON-GOALS
- No new features
- No refactor unless required to fix
- No scope expansion
- No adjacent cleanup unless truly required to fix the bug

F) EVIDENCE (if provided)
- Screenshot / steps / notes / receipt details: ...

G) EXECUTION GATE
- Continue allowed after PASS: <Yes / No>
- If No: wait for explicit planning confirmation or next handoff before later tickets.

H) OUTPUT REQUIREMENTS (keep it short)
- Completed? (Yes/No)
- Build/test status (only what is relevant to this bugfix): <command + PASS/FAIL>
- Root cause (1–3 lines max)
- Fix summary (1–3 bullets)
- What to verify (1–3 bullets; UI-oriented if possible)
- Appendix only if FAIL or explicitly requested

[/BUGFIX TICKET -> Execution Engineer]
```

---

## TEMPLATE: TPL.DELIVERY.EVIDENCE_ONLY.TO_EXECUTOR

```text
[EVIDENCE ONLY -> Execution Engineer]
Request ID: <AUTO_KEY-YYMMDD-NNN-EVID>    // e.g. PASTE-260210-112-EVID
Purpose: collect real current project facts (no guessing; no changes)

A) ALIGNMENT ANCHORS (if relevant)
- Business module: <... / Unknown>
- Route / Entry: <... / Unknown>
- Process node: <... / Unknown>
- Object scope: <... / Unknown>

B) WHY THIS FACT IS NEEDED
- <why delivery is blocked without this fact>
- <what decision depends on it>

C) NEED EVIDENCE (exact items)
- Item 1: <what to verify>
- Item 2: <what to verify>
- Item 3: <what to verify>

D) HOW TO COLLECT (read-only / facts only)
- Commands to run (if any): <...>
- Files / modules / routes / components to inspect: <...>
- UI behavior to verify (if any): <...>

E) OUTPUT REQUIREMENTS
- Short receipt (<=20 lines):
  - Completed? (Yes/No)
  - Findings: <fact + pointer>
  - Not found items (if any): <list>
  - Build/test status only if relevant to the fact
  - 1 blocker question max (only if truly blocking)

F) HARD RULES
- Facts only. No planning. No new requirements.
- No code / behavior changes.
- No formatting-only edits.
- If something cannot be confirmed, write exactly: Not verified

[/EVIDENCE ONLY -> Execution Engineer]
```

---

## TEMPLATE: TPL.DELIVERY.IMPACT_CHECK.TO_EXECUTOR

```text
[IMPACT CHECK -> Execution Engineer]
Request ID: <AUTO_KEY-YYMMDD-NNN-IMP>    // e.g. PASTE-260210-112-IMP
Related Work: <Parent Frozen ID / Step ID / Exec Ticket ID>

A) ALIGNMENT ANCHORS
- Business module: <... / Unknown>
- Route / Entry: <... / Unknown>
- Process node: <... / Unknown>
- Object scope: <... / Unknown>

B) PURPOSE
- Provide a short, fact-based impact and regression watchlist before continuing delivery.

C) PROVIDE
1) Likely impacted areas (very short):
- Pages / flows:
- Modules / files:
- APIs / contracts (if any):

2) Regression watchlist:
- Existing behaviors that could break:
- Critical paths to re-verify:

3) Build/test status:
- Relevant build/test command(s): ...
- PASS/FAIL:
- Any failing tests? (names only, if relevant)

D) OUTPUT REQUIREMENTS
- Max ~30 lines
- Facts only
- No refactor proposals
- No scope expansion
- No planning replacement

[/IMPACT CHECK -> Execution Engineer]
```

---

## TEMPLATE: TPL.DELIVERY.ACCEPTANCE_STEPS.TO_USER_CN

```text
[验收步骤]
（只在你把 Cursor 输出贴回来之后才使用；不新增需求；只允许 UI 点验）

【入口（必须写清）】
- 菜单路径：...
- 页面地址（路由 / 入口锚点）：...

【请按顺序点验（3–8 条，越短越好）】
1) ...
2) ...
3) ...
4) ...

【你应该看到】
- ...（1–3 条即可）

【不应出现】
- ...（1–3 条即可）

请你回复：
- PASS 或 FAIL
- 你看到的关键现象（1–3 句）
[/验收步骤]
```

---

## TEMPLATE: TPL.DELIVERY.CLARIFY.TO_PLANNING

```text
[DELIVERY CLARIFY -> Planning Lead]
Query ID: <ParentID-PQNN>      // e.g. PASTE-260210-112-PQ01
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Related Step(s): <...-01 / ...-02 / ...>
Title: <short>

A) Why execution is blocked (1 line)
- ...

B) Questions (planning-level only; independent; max 1–5)
- Q1: ...
- Q2: ...
- Q3: ...

C) Boundary impact
- Possible boundary change? <Yes / No / Maybe>
- Why: ...

D) What I will do after your reply
- Adjust ticket boundary / pick the correct next ticket / confirm stop point / update split map.

[/DELIVERY CLARIFY -> Planning Lead]
```

---

## TEMPLATE: TPL.DELIVERY.SPLIT_MAP.FULL

```text
[SPLIT MAP — FULL]
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Parent Step ID: <AUTO_KEY-YYMMDD-NNN-01>
Title: <short>

Sub-tickets (engineering execution units):
- <...-01.a> — Purpose: ... — Status: <Planned / Current / Done / Blocked / Cancelled>
- <...-01.b> — Purpose: ... — Status: ...
- <...-01.c> — Purpose: ... — Status: ...

Notes:
- Why this split exists (1–3 lines): ...
- Alignment anchors per sub-ticket: include Business module + Route / Entry + Process node + Object scope
- Unknowns (if any): ...
- Hard Stop / gate note (if any): ...
- Next CURRENT ticket: <...-01.a>

Change log (optional, short):
- <date>: <created / cancelled / merged / re-ordered> <ticket id> because ...

[/SPLIT MAP — FULL]
```
