# Legacy analysis and semantic recovery

## Contents

1. Analysis order
2. Dependency model
3. Business-rule record
4. Semantic traps
5. Review gate

## Analysis order

Analyze from observable entry points inward:

1. Business capability and user/operator trigger.
2. CICS transaction, API, message, file arrival, scheduled job, or manual command.
3. JCL/job flow or online program flow.
4. Program calls, dynamic calls, copybooks, macros, maps, and generated artifacts.
5. Database, VSAM, IMS, MQ, files, reports, and external services.
6. Commit, rollback, restart, error, audit, and operator-recovery behavior.

Do not infer the application boundary from a single high-level qualifier.

## Dependency model

Represent nodes with stable IDs and source evidence:

- Programs and entry points.
- Copybooks, macros, maps, DBDs, PSBs, schemas, and control cards.
- Jobs, PROCs, steps, programs/utilities, scheduler events, conditions, and datasets.
- CICS transactions, programs, files, queues, COMMAREAs, channels, and containers.
- Db2 tables/views, SQL statements, plans/packages, stored procedures, and isolation.
- IMS transactions/databases, VSAM clusters/indexes, MQ queues/topics, and external systems.
- Screens, APIs, reports, input/output files, users/roles, and operational controls.

Represent edges such as `CALLS`, `INCLUDES`, `EXECUTES`, `READS`, `WRITES`, `UPDATES`, `PUBLISHES`, `CONSUMES`, `COMMITS_WITH`, `SCHEDULED_AFTER`, and `SECURED_BY`. Mark static, dynamic, environment-dependent, inferred, and confirmed edges distinctly.

Search for dynamic behavior including variable program names, JCL symbols and overrides, aliasing, table-driven routing, exit programs, scheduler variables, CICS links/xctls, message headers, and environment-specific configuration.

## Business-rule record

Assign a stable ID such as `BR-CUSTOMER-001` and record:

| Field | Content |
|---|---|
| Name | Short business phrase |
| Trigger | Entry point and precondition |
| Inputs | Fields, formats, ranges, and sources |
| Rule | Unambiguous decision/calculation/state transition |
| Outputs/effects | Response, file, message, database, report, audit |
| Exceptions | Invalid, missing, duplicate, timeout, and system failure behavior |
| Transaction | Commit/rollback/checkpoint boundary |
| Evidence | File and line/statement, JCL step, schema, runtime trace, or approved interview |
| Confidence | Confirmed, probable, or unresolved |
| Target mapping | Component/method/contract |
| Tests | Characterization and target test IDs |
| Owner | Approver and approval date |

Never convert a probable or unresolved rule as though it were confirmed.

## Semantic traps

### Data and arithmetic

- Preserve PIC scale, signedness, storage, intermediate precision, `ROUNDED`, truncation, overflow, size errors, and display formatting.
- Resolve `REDEFINES`, `RENAMES`, `OCCURS DEPENDING ON`, level-88 conditions, reference modification, fillers, alignment, and copybook versioning.
- Model spaces, zeros, low values, high values, null indicators, missing fields, and invalid numeric data separately.
- Account for EBCDIC/Unicode collation, uppercase rules, locale, national characters, fixed columns, sequence areas, and trailing spaces.

### Control flow

- Map fall-through, `PERFORM THRU`, `GO TO`, declaratives, condition-name precedence, paragraph ranges, and abnormal exits before refactoring.
- Preserve external side effects and ordering. Do not assume apparently dead code is unreachable without runtime or owner evidence.

### Online transactions

- Recover CICS pseudo-conversation state, COMMAREA/channel/container layouts, syncpoints, task/user context, TS/TD queues, BMS maps, response codes, and recovery.
- Distinguish conversational UI behavior from business rules. Do not expose legacy record layouts as public APIs by default.

### Batch

- Treat JCL, PROCs, scheduler definitions, utilities, dataset disposition, GDGs, return-code conditions, SORT cards, checkpoints, restart steps, rerun rules, and reconciliation reports as application logic.
- Preserve input order, sort/collation, duplicate handling, commit interval, partial-file behavior, and exactly/at-least-once effects.

### Data and integration

- Recover Db2 cursor order, isolation, locks, SQLCODE branches, indicators, stored procedures, package/bind behavior, and commit frequency.
- Recover VSAM key/alternate index, duplicate and browse semantics, status codes, and record layouts.
- Recover IMS hierarchy/navigation, transaction/message formats, and recovery.
- Recover MQ correlation, persistence, ordering, delivery, backout, dead-letter, and idempotency semantics.

## Review gate

Require walkthroughs with:

- A business SME for rule intent and accepted outputs.
- A mainframe SME for platform semantics and operations.
- A data owner for schemas, quality, retention, and reconciliation.
- Security and operations owners for identity, audit, recovery, and nonfunctional behavior.

Record disagreements as decisions or unresolved risks; never resolve them silently in code.
