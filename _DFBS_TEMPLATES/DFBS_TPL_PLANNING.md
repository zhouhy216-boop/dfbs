# DFBS_TPL_PLANNING.md
> Template library for **Planning Lead** only.  
> Locate templates fast via `## TEMPLATE: <TID>` and Ctrl+F.

---

## TEMPLATE: TPL.PLANNING.SPLIT_STEPS.TO_DELIVERY
```text
[PLAN -> Delivery Manager]
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Title: <short title>

A) Steps (product-level, user-visible goals)
- <AUTO_KEY-YYMMDD-NNN-01>: <one-line user-visible goal>
- <AUTO_KEY-YYMMDD-NNN-02>: <one-line user-visible goal>
- <AUTO_KEY-YYMMDD-NNN-03>: <one-line user-visible goal>
- ...

B) Dependencies / Notes
- <...-02> depends on <...-01> because <product reason>
- <...-03> depends on <...-02> because <product reason>
- Unknown: <what is unknown and why it matters>

C) Risk (product-logic only; not engineering)
- Potential conflict with: <other Parent IDs if known>
- Potential missing precondition: <entry/status/permission> (Unknown if not confirmed)

D) Backlog / Todo (full, titles ok but not omitted)
- <item 1>
- <item 2>
- ...

E) Planning Notes (product-level only)
- <1–3 bullets max>
[/PLAN -> Delivery Manager]
```text

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
- From freeze / user artifact: ...
- Unknown / not confirmed: ...

Next action (one):
- Ask user for artifact: <what>
- Or send Clarify Query to Discovery Lead: <Query ID>
[/DEPENDENCY NOTE]
```text

## TEMPLATE: TPL.PLANNING.SNAPSHOT.FULL
```text
[PLANNING SNAPSHOT — FULL]
As-of: <YYYY-MM-DD>
Owner: Planning Lead

A) Active Parent Frozen IDs (from Discovery)
- <AUTO_KEY-YYMMDD-NNN> — <title> — Status: <Planning / Blocked / Ready for Delivery>

B) Step Plan (full)
Parent: <AUTO_KEY-YYMMDD-NNN> — <title>
- <...-01>: <goal> — Status: <Ready / Blocked / Done?> — Blocked by: <... or None>
- <...-02>: <goal> — Status: ...
- <...-03>: <goal> — Status: ...
Notes:
- Dependencies: ...
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
```text

## TEMPLATE: TPL.PLANNING.CLARIFY.TO_DISCOVERY
```text
[PLANNING CLARIFY -> Discovery Lead]
Query ID: <ParentFrozenID-QNN>    // e.g., PASTE-260210-112-Q01
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>
Title: <short>

Why this blocks planning (1 line):
- ...

Questions (independent; max 1–5):
- Q1: ...
- Q2: ...
- Q3: ...

What I will do after your reply:
- Update dependencies/order and refresh the full planning snapshot.
[/PLANNING CLARIFY -> Discovery Lead]
```text

