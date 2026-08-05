# Full-stack verification

Verify the integrated user task, not three isolated technology projects.

## Traceability

Maintain:

```text
legacy evidence
  -> rule/interface/task ID
  -> React route/component/action
  -> OpenAPI operation/error
  -> Spring use case/domain rule
  -> repository/integration adapter
  -> Azure SQL object/migration
  -> unit/contract/integration/E2E/parity test
  -> actual result and approval
```

Every intentional behavior change needs a requirement, owner, test, migration/cutover impact, and rollback consequence.

## Test layers

### React

- Component behavior, forms, messages, route states, permissions, exact value display, focus, keyboard, and accessibility.
- Contract-derived request/response handling and safe errors.

### Spring Boot

- Domain rules, state transitions, validation, authorization, idempotency, concurrency, transactions, batch restart, and integration mappings.
- API schema and error-contract compatibility.

### Azure SQL

- Clean and upgrade migrations, metadata, constraints, indexes, query plans, isolation, locks, deadlocks, data migration, reconciliation, and recovery.
- Run repeatable inner-loop integration tests against local SQL Server and authoritative compatibility/readiness tests against Azure SQL Database.

### Integrated

- Browser-to-database success, invalid, forbidden, conflict, duplicate, partial failure, unavailable, retry, timeout, and recovery paths.
- Differential comparison with approved legacy results.
- External message/file effects and audit/correlation.

## Parity dimensions

Compare:

- Displayed and returned field values.
- Decimal precision, scale, rounding, signs, overflow, and control totals.
- Null, blank, omitted, defaulted, cleared, and unchanged values.
- Code values, status transitions, messages, reason codes, and error priority.
- Sorting, collation, stable ordering, pagination, and duplicate handling.
- Database before/after state, row counts, versions, audit fields, and transactions.
- Files/messages, fixed widths, encodings, header/trailer totals, and side effects.
- Commit, rollback, savepoint, restart, rerun, retry, and concurrency outcomes.
- Authorization and audit for both allowed and denied operations.
- User task completion, keyboard flow, focus, error recovery, and session behavior.

Visual similarity alone is not parity. Approved UX changes may differ visually while preserving or intentionally changing documented task behavior.

## Quality gates

1. **Evidence:** extraction reconciled and rules/tasks traceable.
2. **Contract:** UI, OpenAPI, errors, identity, and data mapping approved.
3. **Component:** frontend, backend, and migration checks pass independently.
4. **Local integrated:** full-stack oracle cases pass against a clean local SQL Server when that option is enabled.
5. **Azure compatibility:** the same migrations and required cases pass against Azure SQL Database.
6. **Nonfunctional:** accessibility, security, performance, resilience, and batch windows pass.
7. **Operational:** monitoring, reconciliation, deployment, recovery, rollback, and support rehearsed.
8. **Cutover:** abort thresholds, observation window, ownership, and legacy fallback approved.

## Evidence record

For every validation run, record:

- Code and source revisions.
- Environment and relevant configuration.
- Local database engine/container image or Azure SQL service configuration, as applicable.
- Sanitized input/oracle case IDs.
- Commands executed and exit codes.
- Actual results and artifact locations.
- Differences and disposition.
- Reviewer and approval status.

Do not claim a test ran when only its code was generated.
