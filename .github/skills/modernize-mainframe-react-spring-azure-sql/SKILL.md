---
name: modernize-mainframe-react-spring-azure-sql
description: "Continue an approved, evidence-backed mainframe roadmap and phase DAG using React with TypeScript, Spring Boot, and Azure SQL Database. Use after discovery approval to refine and execute target architecture, UI workflow design, OpenAPI contracts, Java domain and application code, Azure SQL schema migrations, local SQL Server integration testing, full-stack validation, parity, coexistence, rollback, and cutover nodes. Do not use for initial extraction, inventory, dependency discovery, roadmap creation, or unapproved rule recovery."
---

# Modernize Mainframe to React, Spring Boot, and Azure SQL

Convert business capabilities, not source files or 3270 screens in isolation. Recover observable legacy behavior, approve the target workflow and contracts, then replace one bounded vertical slice across React, Spring Boot, and Azure SQL.

## Operating contract

- Treat `legacy-source/` as immutable evidence.
- Use React, Spring Boot, and Azure SQL Database as the only implementation stack. Do not create a parallel target implementation.
- Keep React presentation concerns, Spring application/domain rules, and persistence/integration concerns separate.
- Keep the browser independent of Azure SQL, mainframe protocols, database models, and internal Spring types. The browser communicates only through approved HTTP/event contracts.
- Keep authoritative validation, calculations, state transitions, authorization, and transaction decisions in the Spring application/domain tiers. React may provide non-authoritative usability validation.
- Target Azure SQL Database, not Azure SQL Managed Instance or generic on-premises SQL Server.
- Permit a local SQL Server instance or container for development and automated tests only. Run the same migrations and SQL dialect locally; never treat local SQL Server success as proof of Azure SQL Database compatibility.
- Preserve observable behavior unless an approved requirement intentionally changes it.
- Never invent missing copybooks, map definitions, schemas, code mappings, transaction boundaries, navigation behavior, failure behavior, or external contracts.
- Require a stable rule or interface ID, legacy evidence citation, target mapping, and test for every recovered behavior.
- Implement one reviewable vertical slice at a time. Keep database, API, frontend, deployment, and rollback changes traceable to that slice.
- Continue the discovery-created roadmap and DAG. Refine approved target nodes in place, preserve completed/superseded history, and never create a disconnected implementation plan.
- Stop the affected work when evidence is missing or conflicting. Record the gap, impact, owner, and evidence needed.

## Required inputs

Locate or obtain:

- An approved discovery handoff under `modernization/handoff/` for the bounded slice.
- The current `modernization/plan/modernization-roadmap.md`, `phase-dag.json`, and `status.md`, with the handoff's predecessor nodes completed and its target-design node eligible to become `ready`.
- Business, UX, mainframe, data, security, accessibility, operations, and target-platform owners.
- In-scope transactions, screens, jobs, interfaces, datasets, tables, queues, and scheduler events.
- Reconciled extraction manifests, source revisions, encodings, record formats, and exclusions.
- COBOL/PL/I/Assembler, copybooks, BMS/MFS maps, CICS/IMS definitions, JCL/PROCs, Db2 DDL/SQL, VSAM layouts, MQ definitions, and file layouts.
- Sanitized legacy inputs, outputs, database changes, messages, files, return codes, failures, restarts, and concurrency cases.
- Volumes, latency, batch windows, availability, recovery, audit, privacy, retention, and accessibility requirements.
- Approved LTS JDK, Spring Boot baseline, Node.js baseline, React toolchain, package governance, hosting, identity, observability, CI/CD, Azure SQL constraints, and local container/runtime policy.

Record unavailable inputs as risks. Continue only where the gap cannot change the result.

## Repository contract

Use these separate locations:

```text
modernization/react-spring-azure-sql/
  analysis/
  architecture/
  plans/
  evidence/

target/react-spring-azure-sql/
  frontend/
  backend/
  database/
  tests/e2e/
```

The cross-phase roadmap remains under `modernization/plan/`. Detailed target work packets live under `modernization/react-spring-azure-sql/plans/` and must reference and refine their parent DAG node.

Use `modernization/evidence/characterization/` as the approved legacy oracle when it already exists. Never copy generated target code into `legacy-source/`.

Read [deliverables.md](./references/deliverables.md) and the canonical roadmap/DAG/ledger contract in [the discovery deliverables reference](../modernize-mainframe-application/references/deliverables.md) before creating artifacts or evaluating state.

## Workflow

### 1. Validate the approved evidence handoff

1. Confirm the handoff names the application, environment, source revision, owners, bounded entry points, and exclusions.
2. Read `phase-dag.json` and `status.md`, validate and replay canonical ledger records in sequence order, reconcile the cached statuses against the derived state, and stop as blocked on any malformed record, conflict, or mismatch before evaluating readiness.
3. Locate its DAG approval node; verify all predecessors are `completed`, its structured approval record matches every canonical approval field and the source, handoff, scope, and roadmap revisions, and the design milestone is the next eligible phase.
4. Verify extraction reconciliation, encoding, dependency, rule, interface, data, transaction, restart, failure, and external-effect evidence is cited and approved.
5. Verify the executable oracle covers representative, boundary, invalid, duplicate, unauthorized, concurrency, failure, restart, and retry scenarios with approved normalization and tolerances.
6. Read [legacy-ui-recovery.md](./references/legacy-ui-recovery.md) only to map approved terminal evidence into target user tasks; do not reopen broad legacy discovery.
7. Stop affected target work and return the gap to `modernize-mainframe-application` when evidence is missing, conflicting, or unapproved. Record the relevant node as `blocked`; do not start or transition a target work node before this validation passes.

Gate: the bounded handoff is approved and every in-scope behavior is evidenced or explicitly blocked.

### 2. Approve the full-stack design

1. Write and approve the slice expansion manifest, then expand its stable target milestones with reviewable design, contract, domain, database, API, frontend, integration, parity, rollback, and cutover child nodes. Follow the canonical `<milestone-id>.<stage>.<outcomeKey>` ID rule, manifest canonicalization and digest, explicit edges, duplicate detection, conflict blocking, `parentId`, separate readiness/completion criteria, and explicit approval nodes. Do not execute milestones, replace their IDs, rewire unrelated branches, rename executed nodes, or delete prior history.
2. Select the first `ready` design work node, append its `in-progress` transition to the ledger, then update its cached status.
3. Record an ADR accepting React, Spring Boot, and Azure SQL Database for the target slice.
4. Define presentation, application/domain, and data/integration tier responsibilities.
5. Map legacy user tasks to React routes, pages, forms, actions, states, and accessibility behavior.
6. Define OpenAPI contracts, error contracts, authentication/authorization, idempotency, concurrency, versioning, and correlation.
7. Decide SPA-with-API versus backend-for-frontend using the threat model and identity requirements.
8. Define Azure SQL ownership, schema, data migration, transaction strategy, coexistence, reconciliation, and recovery.
9. Prefer a modular monolith unless independent deployment or scaling is justified.
10. Map every target component and contract to legacy rule and test IDs.

Read [target-architecture.md](./references/target-architecture.md), [react-frontend.md](./references/react-frontend.md), [spring-azure-sql.md](./references/spring-azure-sql.md), and [azure-sql.md](./references/azure-sql.md).

Gate: workflow, contracts, threat model, data design, deployment, migration, rollback, and operational model are approved.

### 3. Design contracts and data before implementation

1. Create screen/task-to-route and legacy-field-to-UI mappings.
2. Create the OpenAPI contract before implementing frontend and backend independently.
3. Represent business decimals as JSON strings unless an approved contract proves another lossless representation.
4. Represent dates and times with explicit ISO formats and documented time-zone semantics.
5. Create the Db2/VSAM/file-to-Azure-SQL source-to-target map before DDL.
6. Resolve collation, Unicode, `char` padding, null versus blank, timestamp, identity, key, constraint, index, locking, isolation, ordering, and restart semantics.
7. Generate versioned Azure SQL migrations only after the physical design is approved.

Gate: UI, API, error, security, and database contracts are reviewable, traceable, and approved.

### 4. Implement one vertical slice

For each work packet:

1. Select the next `ready` executable DAG node, append its `in-progress` transition to the ledger, update its cached status, and list its in-scope legacy artifacts, entry points, rules, interfaces, data, oracle cases, and intentional changes.
2. Add or refine characterization and contract tests first.
3. Implement Spring domain types and rules without Spring, HTTP, ORM, or Azure SDK dependencies.
4. Implement application orchestration, authorization policies, idempotency, and explicit transactions.
5. Implement Azure SQL migrations and repository/integration adapters.
6. Implement the OpenAPI endpoint and error behavior.
7. Generate or validate TypeScript API types from the approved contract.
8. Implement the accessible React workflow without duplicating authoritative business rules.
9. Add unit, component, integration, contract, accessibility, end-to-end, security, concurrency, performance, and parity tests.
10. Read [local-sql-testing.md](./references/local-sql-testing.md) and run database integration tests against a clean local SQL Server where supported.
11. Apply the complete database migration chain to an empty approved test database.
12. Run the smallest relevant checks, then the full required suite.
13. Update traceability, risks, ADRs, runbooks, reconciliation, and rollback in the same work packet.
14. Record commands and actual results, mark the node `completed` only when its completion criteria pass, derive parent milestone status, then promote executable nodes whose dependencies and readiness criteria are satisfied to `ready`. Leave pending human decisions as ready approval nodes; mark evidence failures blocked rather than skipping them.

Do not combine unrelated screens, batch paths, database redesigns, interface replacements, or infrastructure changes.

#### Program-group sequencing and recurring pitfalls

When a legacy program group (for example an online inquiry, its validator, and a batch that calls the same validator) is modernized across several slices, apply these session-proven rules:

- Sequence each program as pure rules first, then persistence and orchestration, then the endpoint, then the database integration test. Bind the approved characterization CSVs into the rule test as exact expected values (identifier formats, `HALF_UP` scale-2 rounding, version transitions, family caps, and return codes) before writing any adapter.
- Extract shared legacy subprograms once as dependency-free domain classes and reuse them across the online and batch slices, rather than reimplementing the rules per slice.
- Guard against schema drift. An early reduced-scope slice often creates a narrower table than the legacy DDL; a later slice that needs the omitted columns must add them in its own migration and keep the migration chain the schema source. A missing column, schema-qualifier mismatch, or reduced type surfaces only as a generic 5xx from the API error handler, never as a clear message, so confirm the actual database columns before writing persistence SQL and diff the target schema against the legacy DDL for the whole program, not just the current slice.
- Preserve batch execution semantics exactly: separate-commit run header, rollback on technical failure with the run marked failed, optimistic version checks that require exactly one affected row, and the legacy return-code ladder (clean, business-exception, technical-failure).
- Make every unexpected-exception handler log the cause and correlation id. A handler that swallows the cause turns a one-line schema or constraint error into repeated rebuild-and-probe cycles.
- Verify readiness and parity through the real endpoint that also touches the database, then diff persisted rows against the approved post-state fixtures; a passing unit test plus a green port check is not database parity.

### 5. Verify the integrated slice

Read [full-stack-verification.md](./references/full-stack-verification.md) and [production-readiness.md](./references/production-readiness.md).

Require:

- Rule-to-UI/API/domain/database/test traceability.
- Contract compatibility between React and Spring Boot.
- Differential parity for approved legacy cases.
- Exact numeric, null/blank, encoding, sorting, transaction, restart, and failure behavior.
- Keyboard, focus, labeling, error announcement, contrast, responsive, and supported-browser evidence.
- Authentication, authorization, CSRF/CORS as applicable, input validation, output encoding, dependency, secret, and audit evidence.
- Query-plan, concurrency, connection-pool, transient-fault, performance, batch-window, and recovery evidence.
- Local SQL Server inner-loop results plus an Azure SQL Database compatibility and pre-production validation result.
- Correlated logs, metrics, traces, business reconciliation, protected-data redaction, and runbooks.

Never call the slice equivalent or production-ready while critical mismatches or missing approvals remain.

### 6. Cut over incrementally

1. Select strangler routing, parallel run, coexistence, CDC, shadowing, or batch cutover by interface and consistency needs.
2. Rehearse deployment, data migration, database recovery, restart, reconciliation, frontend rollback, API rollback, and legacy fallback.
3. Define abort thresholds, observation windows, owners, and decision authority.
4. Compare legacy and target outcomes during the approved period.
5. Decommission only after retention, audit, legal hold, support, and rollback requirements are satisfied.

## Cross-tier safeguards

- Never connect React directly to Azure SQL or expose database credentials to the browser.
- Never use JavaScript binary floating point for authoritative monetary calculations.
- Never duplicate state-transition rules in React and Spring Boot.
- Never expose JPA entities or database rows as public API contracts.
- Never let ORM automatic schema generation own production schemas.
- Never map Db2 `TIMESTAMP` to SQL Server `timestamp`; use approved `datetime2` semantics.
- Never replace a legacy numeric version with `rowversion` without an approved behavior change.
- Never infer user authorization from hidden or disabled UI controls; enforce it in Spring Boot.
- Never place access tokens, secrets, production records, or protected data in source, browser storage, logs, snapshots, fixtures, or prompts.
- Never use Azure SQL Managed Instance or on-premises SQL Server-only features.
- Never introduce a local-only SQL branch, alternate migration chain, H2 dialect, or production `trustServerCertificate=true` setting.

## Response format

At the end of each task, report:

1. Slice, entry points, and target components.
2. Legacy evidence inspected and artifacts changed.
3. Rule, interface, screen/task, and data mappings covered.
4. Commands and tests actually run with results.
5. UI, API, business, database, and operational parity mismatches.
6. Risks, assumptions, decisions, and approvals required.
7. Roadmap revision, DAG node/status transition, rollback status, and next unblocked node.

## Resource routing

- Read [legacy-ui-recovery.md](./references/legacy-ui-recovery.md) before translating CICS/BMS, IMS/MFS, 3270, or terminal workflows.
- Read [target-architecture.md](./references/target-architecture.md) before defining tier, module, deployment, security, integration, or coexistence boundaries.
- Read [react-frontend.md](./references/react-frontend.md) before designing or implementing React, TypeScript, browser security, accessibility, or frontend tests.
- Read [spring-azure-sql.md](./references/spring-azure-sql.md) before implementing Spring Boot, OpenAPI, transactions, batch, persistence, migrations, or Azure SQL.
- Read [azure-sql.md](./references/azure-sql.md) before approving source-to-target maps, physical schema, migrations, type mappings, collation, isolation, or database readiness.
- Read [local-sql-testing.md](./references/local-sql-testing.md) before configuring Testcontainers, Docker Compose, local SQL Server profiles, local credentials, database reset, or local integration tests.
- Read [full-stack-verification.md](./references/full-stack-verification.md) before planning parity, accessibility, security, performance, readiness, or cutover evidence.
- Read [production-readiness.md](./references/production-readiness.md) before production-readiness, differential-parity, nonfunctional, or cutover approval.
- Read [deliverables.md](./references/deliverables.md) before creating analysis, architecture, plan, target, or evidence artifacts.
