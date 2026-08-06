# Legacy UI and task recovery

Recover user intent and observable task behavior before designing React pages. Do not reproduce a terminal screen pixel for pixel unless an approved requirement demands it.

## Evidence to inspect

- BMS/MFS source maps and generated symbolic maps.
- CICS/IMS transaction and program definitions.
- COBOL/PL/I screen handling, COMMAREAs, channels/containers, SPA areas, and message formats.
- AID/PF/PA keys, cursor position, map-fail handling, MDT/FSET behavior, protected/unprotected fields, autoskip, field attributes, and length rules.
- Pseudo-conversational state, terminal return, timeout, cancel, back, refresh, and session-expiry behavior.
- Authentication context, user IDs, roles, resource checks, audit, and operator procedures.
- Screenshots, manuals, help text, training material, accessibility accommodations, and approved user observation.

Do not infer behavior from the map alone. Cite the program path and runtime evidence that handles each action.

## Required recovery artifacts

Create:

- `screen-inventory.csv`: screen/map, transaction, program, purpose, actor, entry condition, exit actions, evidence.
- `screen-flow.mmd`: states, actions, decisions, messages, next states, cancel and failure paths.
- `field-map.csv`: legacy map/field, meaning, type, length, input/output/protected, validation, codes, sensitivity, target control, evidence.
- `action-map.csv`: AID/PF action, precondition, rule IDs, effects, messages, target action, API operation, evidence.
- `message-catalog.csv`: message/code, condition, severity, target error contract, user announcement, audit behavior.
- `task-to-route-map.csv`: business task, React route/page, API use case, authorization, completion outcome, oracle cases.

## Recovery rules

- Distinguish blank, null, low values, defaulted, omitted, unchanged, and cleared fields.
- Preserve significant leading zeroes, fixed-width identifiers, code values, maximum lengths, and decimal scale.
- Recover when validation occurs: keystroke, field exit, submit, server response, or batch processing.
- Record first-focus, cursor placement, field highlighting, message placement, and resubmission behavior when they affect task completion.
- Identify hidden state stored in COMMAREA, temporary storage, IMS SPA, terminal state, database state, and external systems.
- Recover authorization and audit independently from whether a field or action is visible.
- Treat PF keys as business actions, navigation shortcuts, or terminal-only controls based on evidence.
- Preserve task outcomes and side effects. A React workflow may consolidate or split legacy screens only through an approved UX decision.

## React mapping guidance

Map:

- Protected output fields to text or read-only controls based on semantics, not appearance.
- Editable fields to typed controls with accessible labels, descriptions, constraints, and error association.
- PF/AID actions to clearly named buttons, links, routes, or shortcuts.
- Pseudo-conversational states to explicit URL, application, server, or persisted state with defined refresh/back behavior.
- Map-level messages to an accessible summary plus field-level errors where supported by evidence.
- Terminal exits to explicit cancel, sign-out, return, or close-task behavior.

Do not expose raw COMMAREAs, copybook structures, EIB fields, map field names, or mainframe response codes to React.

## Approval gate

Require business and UX approval for:

- Task and navigation changes.
- Consolidated or removed fields/screens.
- Changed validation timing or wording.
- Changed keyboard shortcuts, focus, timeout, or cancel behavior.
- Accessibility improvements that intentionally change presentation while preserving business outcomes.
- Any legacy behavior deliberately corrected rather than preserved.
