Recommended project location: docs/product/modules/M02_contract_review/
Recommended file name: CONREV_V1_NEW_TEMPLATE.normalized.md
Recommended full path: docs/product/modules/M02_contract_review/CONREV_V1_NEW_TEMPLATE.normalized.md

# Contract Review V1 — New Template Only
## Normalized Module Source

## Module name
- M02 Review Collaboration / Contract Review
- Current document scope: Contract Review V1 (New Template Only)

## Purpose / user-visible goal
- Let Sales start a contract review from the review list.
- Let Business Planning receive the review, open the full form, edit the whole form in V1, and decide Pass / Return / Close.
- Preserve real business ownership meaning:
  - who initiated
  - who is assigned
  - who is currently handling
- Preserve clear action history and review notes in UI.
- Carry the review form on top of the form wheel, not as a one-off hardcoded review-only form system.

## Entry / route anchor(s)
- Module anchor: M02 Review Collaboration
- Current entry anchor:
  - Contract Management -> Review List -> Start Review
- Current route anchor:
  - independent route/page anchor not yet confirmed
- Start interaction:
  - popup-based start flow

## Roles involved
- Sales
- Business Planning

## Main object(s)
- Contract review record
- Review form instance
- Review action record
- Initiator
- Assigned Business Planning person
- Current handler
- Review notes / reasons
- Review result / status

## Main flow
1. Sales starts a review from Contract Management -> Review List.
2. System opens the new-template contract review start flow by popup.
3. A review record is created and assigned to Business Planning.
4. Business Planning opens the review item.
5. Business Planning reviews and can edit the whole form in V1.
6. Business Planning chooses one of:
   - Pass
   - Return
   - Close
7. If returned:
   - Sales can edit and resubmit
   - or Close
8. After Pass in V1:
   - only the review result/status is updated
   - no automatic downstream stage creation is triggered

## States
- Draft / started
- In review
- Returned
- Closed
- Passed

User-visible required pass label:
- 评审通过

## Actions
### Sales
- Start Review
- Open own initiated review
- Edit after Return
- Resubmit after Return
- Close after Return

### Business Planning
- Open assigned review
- Edit whole form in V1
- Pass
- Return
- Close

## Ownership / visibility semantics
- “My initiated items” must preserve real initiator meaning.
- “Assigned to me” must preserve real assignee meaning.
- Do not collapse V1 into a generic role-only pool.
- V1 must preserve at minimum:
  - initiator
  - assigned Business Planning
  - current handler
  - action-history actor

## Audit / history requirements
V1 must preserve and show:
- initiator
- current handler
- assigned Business Planning
- timestamps
- action history
- review notes / reasons

Audit/history is required user-visible value, not optional metadata.

## Related modules / dependencies
### Related modules
- M01 Contract & Customer
- M02 Review Collaboration
- M03 Production Preparation
- M08 Platform & SIM collaboration

### Dependency meaning
- V1 depends on real identity semantics:
  - initiator
  - assignee
  - current handler
- V1 should be carried by the form wheel.
- Existing relevant reusable capabilities should be reused first.
- If an existing capability works as-is or with a small patch, do not create a parallel review-only mechanism.

## V1 scope
- New template only
- Start from Contract Management -> Review List
- Popup-based start flow
- Business Planning is the only review node in V1
- Business Planning can edit the whole form in V1
- Supported outcomes:
  - Pass
  - Return
  - Close
- Returned item can be edited and resubmitted by Sales
- Full audit trail is required
- Extra service-related items in the review form are part of saved review data in V1
- Contract category A / B / C is recorded as form data in V1
- Contract category A / B / C does not change workflow logic in V1
- V1 must be built on the form wheel
- V1 may start with whole-form editable behavior for Business Planning
- But the carried structure must not block later upgrade to:
  - field-level editable control
  - group-level editable control

## Non-goals
- No change-review flow in current V1
- No multi-level approval chain in current V1
- No division head / management / VP / GM review chain in current V1
- No automatic downstream production-preparation / training / platform / freight actions after Pass in current V1
- No generic role-pool-only visibility model
- No separate contract-review-only form engine
- No requirement that field-level or group-level editability must already be enforced in V1

## Unknowns
- Independent route/page anchor for M02 is still not confirmed.
- Exact list structure for “my initiated / assigned to me / handled by me” is not yet frozen.
- Exact final wording/UX details of Close are not yet frozen.
- Exact field/group editability matrix for later stages is not part of current V1.
- Exact reuse classification of every existing related wheel/component still needs fact-by-fact confirmation during implementation planning/execution.

## Source references
### Long-lived project sources
- PROJECT_COLLAB_CHARTER_v1.md
- MODULE_ROUTE_ANCHORS_v0.1.md
- BUSINESS_MAP_v0.1.md
- PROCESS_MAP_v0.1.md
- OBJECT_MAP_v0.1.md

### Current-round user-confirmed wording
- Contract Review V1 is new template only.
- Change review is a separate later flow on top of an already-passed application/review.
- Current desired relationship:
  - contract review is the current business-driving consumer
  - it should be carried by the form wheel
  - existing related wheels/capabilities should be reused whenever possible
  - do not grow a parallel review-only mechanism

### Related current-stage references
- THREAD_表单自定义轮子.md
- current-stage account / identity / role semantics evidence
- current-stage contract-review-not-in-repo evidence