# DFBS_TPL_DELIVERY.md
> Template library for **Delivery Manager (C)** only.  
> Locate templates fast via `## TEMPLATE: <TID>` and Ctrl+F.

---

## TEMPLATE: TPL.DELIVERY.TICKET.TO_EXECUTOR
```text
[EXECUTION TICKET -> Execution Engineer]
Ticket ID: <AUTO_KEY-YYMMDD-NNN-STEP.SUB>     // e.g., PASTE-260210-112-01.a
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Parent Step ID: <AUTO_KEY-YYMMDD-NNN-01>
Title: <short>

GOAL (user-visible):
- <plain user-visible outcome>

SCOPE (do now):
- ...

NON-GOALS (explicitly NOT now):
- ...

CONSTRAINTS (must/never):
- Must: ...
- Must: keep WIP=1 (this ticket only)
- Must: keep output short (see OUTPUT REQUIREMENTS)
- Never: implement anything outside Scope
- Never: change unrelated behavior

UNKNOWNs (do not guess):
- ...

IMPLEMENTATION NOTES (engineering-facing, still concise):
- <where to change / expected approach / files/areas if known>
- If uncertain, ask 1 blocker question only.

OUTPUT REQUIREMENTS (keep it short):
- Completed? (Yes/No)
- Full-suite build PASS/FAIL (command used)
- Changed areas (very short: pages/modules/files)
- What to verify (1–3 bullets)
- 1 blocker question max (only if truly blocking)
- Appendix only if FAIL or explicitly requested

[/EXECUTION TICKET -> Execution Engineer]
```text

## TEMPLATE: TPL.DELIVERY.BUGFIX.TICKET.TO_EXECUTOR
```text
[BUGFIX TICKET -> Execution Engineer]
Ticket ID: <AUTO_KEY-YYMMDD-NNN-STEP.SUB-fixN>   // e.g., PASTE-260210-112-01.a-fix1
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Parent Step ID: <AUTO_KEY-YYMMDD-NNN-01>
Related Exec Ticket: <AUTO_KEY-YYMMDD-NNN-01.a>
Title: <short>

BUG (observed):
- User observed: ...
- Expected: ...
- Actual: ...

SCOPE (fix only):
- Fix the described behavior to match Expected.
- Keep the original goal unchanged.

NON-GOALS:
- No new features
- No refactor unless required to fix
- No scope expansion

CONSTRAINTS:
- Must: avoid regressions
- Must: keep output short (see OUTPUT REQUIREMENTS)

EVIDENCE (if provided):
- Screenshot / steps / notes: ...

OUTPUT REQUIREMENTS (keep it short):
- Completed? (Yes/No)
- Full-suite build PASS/FAIL (command used)
- Root cause (1–3 lines max)
- Fix summary (1–3 bullets)
- What to verify (1–3 bullets)
- Appendix only if FAIL or explicitly requested

[/BUGFIX TICKET -> Execution Engineer]
```text

## TEMPLATE: TPL.DELIVERY.EVIDENCE_ONLY.TO_EXECUTOR
```text
[EVIDENCE ONLY — DO NOT MODIFY ANYTHING]
Request ID: <AUTO_KEY-YYMMDD-NNN-EVID>    // e.g., PASTE-260210-112-EVID
Purpose: collect real project facts (no guessing)

Need evidence (exact items):
- Item 1: <what + expected location (paths/pages) if known>
- Item 2: ...
- Item 3: ...

How to collect (if applicable):
- Commands to run (read-only): ...
- Where to look (files/modules): ...
- How to reproduce UI behavior: ...

Output requirements:
- Short receipt (<=20 lines):
  - Completed? (Yes/No)
  - Findings: <what you found + where (file paths / UI entry / command)>
  - Not found items (if any): <list>
  - Full-suite build PASS/FAIL (command used)
  - 1 blocker question max (only if truly blocking)
- Appendix (optional):
  - Only include logs/snippets if FAIL or explicitly requested.

Prohibited:
- No refactor
- No formatting
- No behavior change
- No new features
- No dependency upgrades
[/EVIDENCE ONLY]
```text

## [IMPACT CHECK — FACTS ONLY]
```text
Request ID: <AUTO_KEY-YYMMDD-NNN-IMP>    // e.g., PASTE-260210-112-IMP
Related Work: <Parent Frozen ID / Step ID / Exec Ticket ID>

Goal:
- Provide a short, fact-based impact + regression checklist BEFORE implementation.

Provide:
1) Likely impacted areas (very short):
- Pages/flows:
- Modules/files:
- APIs/contracts (if any):

2) Regression watchlist (must-test suggestions):
- Existing behaviors that could break:
- Critical paths to re-verify:

3) Build/test status:
- Full-suite build PASS/FAIL (command used)
- Any failing tests? (list names only)

Output requirements (short):
- Max ~30 lines
- No refactor proposals
- No scope expansion
[/IMPACT CHECK — FACTS ONLY]
```text

## TEMPLATE: TPL.DELIVERY.ACCEPTANCE_STEPS.TO_CEO_CN
```text
[验收步骤]
（只在你把 Cursor 输出贴回来之后才使用；不新增需求）

请按顺序点验：
1) ...
2) ...
3) ...
4) ...
（共 3–8 条，越短越好）

验收口径：
- 你看到的现象应当是：...
- 不应出现：...

请你回复：
- PASS 或 FAIL
- 你看到的关键现象（1–3 句）
[/验收步骤]
```text

## TEMPLATE: TPL.DELIVERY.CLARIFY.TO_PLANNING
```text
[DELIVERY CLARIFY -> Planning Lead]
Query ID: <ParentID-PQNN>      // e.g., PASTE-260210-112-PQ01
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Related Step(s): <...-01 / ...-02 / ...>
Title: <short>

Why execution is blocked (1 line):
- ...

Questions (planning-level only; independent; max 1–5):
- Q1: ...
- Q2: ...
- Q3: ...

What I will do after your reply:
- Adjust ticket boundaries / pick the correct next step / update split map.
[/DELIVERY CLARIFY -> Planning Lead]
```text

## TEMPLATE: TPL.DELIVERY.SPLIT_MAP.FULL
```text
[SPLIT MAP — FULL]
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Parent Step ID: <AUTO_KEY-YYMMDD-NNN-01>
Title: <short>

Sub-tickets (engineering execution units):
- <...-01.a> — Purpose: ... — Status: <Planned/Current/Done/Blocked/Cancelled>
- <...-01.b> — Purpose: ... — Status: ...
- <...-01.c> — Purpose: ... — Status: ...

Notes:
- Why this split exists (1–3 lines): ...
- Unknowns (if any): ...
- Next CURRENT ticket: <...-01.a>

Change log (optional, short):
- <date>: <created/cancelled/merged> <ticket id> because ...
[/SPLIT MAP — FULL]
```text

