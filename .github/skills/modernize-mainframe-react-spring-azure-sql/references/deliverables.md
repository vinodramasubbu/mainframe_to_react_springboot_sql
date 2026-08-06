# React-Spring-Azure-SQL deliverables

Create only artifacts required for the current phase.

Continue the application roadmap in `modernization/plan/`. Every target artifact and work packet must reference its parent node from `phase-dag.json`; update `status.md` with actual execution evidence and expand only the approved slice branch.

## Modernization evidence

Use:

```text
modernization/react-spring-azure-sql/
  analysis/
    system-context.md
    dependency-graph.mmd
    entry-points.csv
    business-rules.csv
    data-dictionary.csv
    screen-inventory.csv
    screen-flow.mmd
    field-map.csv
    action-map.csv
    message-catalog.csv
    task-to-route-map.csv
    unknowns-and-risks.csv
  architecture/
    target-architecture.md
    openapi.yaml
    error-contract.md
    identity-and-threat-model.md
    database/
      source-to-target-map.csv
      migration-plan.md
      schema-verification.md
    adrs/
  plans/
    work-packets/
      <slice-id>.md
      <slice-id>-expansion.json
    requirements-traceability.csv
    migration-backlog.csv
    cutover.md
    rollback.md
    decommission.md
  evidence/
    contracts/
    parity/
    accessibility/
    security/
    performance/
    resilience/
    operations/
```

Reuse approved legacy oracle cases from `modernization/evidence/characterization/`; do not rewrite expected results to match the target.

## Target implementation

Use:

```text
target/react-spring-azure-sql/
  frontend/
    src/
      app/
      features/
      shared/
      generated/api/
    tests/
  backend/
    src/main/java/
    src/main/resources/
      db/migration/
    src/test/
  database/
    migration-support/
    local/
      compose.yaml
      env.example
    reconciliation/
  tests/e2e/
```

Keep the authoritative versioned schema migrations in one approved location. Do not maintain duplicate migration chains under both `backend/` and `database/`.

Create the optional `database/local/` files only when local SQL Server testing is selected. Never commit a populated `.env`, passwords, persistent database files, or production-derived data.

## Work-packet template

```text
Roadmap revision:
Parent DAG node:
Node status transition:
Capability/slice:
Actors and entry points:
Legacy artifacts and revision:
Screen/task IDs:
Business-rule IDs:
Interfaces and data:
Oracle/test IDs:
Intentional behavior changes:
React components/routes:
OpenAPI operations/errors:
Spring use cases/domain rules:
Azure SQL objects/migrations:
External integrations:
Commands and actual results:
Parity mismatches:
Accessibility/security/operations impact:
Rollback:
Risks and assumptions:
Approval node ID:
Accountable role:
Approver identity:
Decision: approved | approved-with-conditions | rejected
Decision timestamp:
Approved scope/slice:
Source revision:
Handoff revision:
Roadmap revision:
Conditions and expiry:
Evidence/reference:
Next unblocked node:
```

Repeat the full approval block for every required target-stage decision. A free-text approval note does not complete an approval node.

`plans/work-packets/<slice-id>-expansion.json` is the approved canonical expansion manifest. Generate child IDs, dependencies, expansion revision, and digest from it using the discovery deliverables contract; do not derive a second graph from prose in the work packet.

## Rollback authority

`plans/rollback.md` is the authoritative versioned rollback plan. DAG rollback nodes reference its exact revision and digest in `requiredInputs`; rollback approvals reference the same revision. The work-packet `Rollback` field and coordinator report only summarize that plan. A revision or digest mismatch blocks validation, cutover readiness, and rollback approval until reconciled.

## Minimum ADRs

Record decisions for:

- React toolchain and frontend architecture.
- Spring Boot runtime and backend structure.
- API/error/versioning contract.
- Identity flow and browser/API security.
- Azure SQL physical design, compatibility level, collation, and isolation.
- Persistence and migration tool.
- Local SQL Server test method, container/runtime policy, image pinning, reset strategy, and Azure compatibility gate.
- Transactions, idempotency, concurrency, and external side effects.
- Batch and scheduler.
- Observability, hosting, deployment, coexistence, and cutover.
