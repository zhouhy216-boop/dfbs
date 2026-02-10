# DFBS_TPL_DISCOVERY.md
> Template library for **Discovery Lead** only.  
> Locate templates fast via `## TEMPLATE: <TID>` and Ctrl+F.

---

## TEMPLATE: TPL.DISCOVERY.FREEZE.TO_PLANNING
```text
[DISCOVERY FREEZE -> Planning Lead]
ID: <AUTO_KEY-YYMMDD-NNN>
Title: <short title>

User-visible goal (plain words):
- ...

Trigger / Where:
- ...

Scope (do now):
- ...

Non-goals (explicitly NOT now):
- ...

Unknowns (not confirmed yet):
- ...

Wheel check:
- Might be a reusable wheel? <Yes/No/Maybe> (user decided)

Minimum “Looks Good” (how user will judge in UI):
- ...

Evidence needed (optional, only if truly required):
- ...

Notes for planning (product-level dependency hints only):
- ...
[/DISCOVERY FREEZE -> Planning Lead]
```text

## TEMPLATE: TPL.DISCOVERY.EVIDENCE_ONLY.TO_EXECUTOR
```text
[EVIDENCE ONLY — DO NOT MODIFY ANYTHING]
Request ID: <AUTO_KEY-YYMMDD-NNN>
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
  - Findings (bullet list): <what you found + where (file paths / UI entry / command)>
  - Not found items (if any): <list>
  - Full-suite build: PASS/FAIL (command used)
  - 1 blocker question max (only if truly blocking): <question>
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

## TEMPLATE: TPL.DISCOVERY.ARCHIVE.INDEX_ADD_LINE
```text
Paste one full Markdown table row into _DISCOVERY/INDEX.md under the index table.

| `THREADS/THREAD_<需求名>.md` | <标题> | 暂停 | <一句话说明> | <下次继续需要的实物> |
```text

## TEMPLATE: _DISCOVERY/THREADS/THREAD_<需求名>.md (full file content)
```text
# THREAD：<标题>
状态：暂停（原因：<为什么暂停/前置依赖是什么/等什么材料>）

## 0) 目的（给未来的我/你）
- 记录这次讨论已经明确的部分，避免下次从头聊。
- 下次继续时：只推进【未确认】与【下次继续需要的实物】；【已冻结】不再反复讨论。

---

## 1) 已冻结（不再反复讨论）
- ...

---

## 2) 已提供的实物/背景（如果有）
- ...

---

## 3) 未确认（Unknown / 下次继续重点）
> 以下不允许脑补；需要实物或用户明确口径。
- ...

---

## 4) 下次继续需要的实物（最省沟通版）
- ...

---

## 5) 下次续聊的切入点（避免重复劳动）
- ...
```text

## TEMPLATE: TPL.DISCOVERY.REPLY_TO_PLANNING_CLARIFY
```text
[DISCOVERY REPLY -> Planning Lead]
Query ID: <ParentFrozenID-QNN>    // e.g., PASTE-260210-112-Q01
Parent Frozen ID: <AUTO_KEY-YYMMDD-NNN>

A) Direct Answers (match B questions)
- A1 (re: Q1): ...
- A2 (re: Q2): ...
- A3 (re: Q3): ...

B) Clarified Freeze Facts (only if needed; no scope expansion)
- Trigger / Where: ...
- Scope: ...
- Non-goals: ...
- Minimum “Looks Good”: ...

C) Unknowns (explicit)
- Unknown 1: ...
- Unknown 2: ...

D) Evidence Needed (to resolve Unknowns; no guessing)
- From user: <screenshots / samples / links>
- If local truth required: suggest Evidence-only via Cursor (no code changes)

E) Planning Note (1–2 lines max)
- <how this affects step order/dependencies>
[/DISCOVERY REPLY -> Planning Lead]


