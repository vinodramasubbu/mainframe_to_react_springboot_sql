# Verification and production-readiness gates

## Contents

1. Evidence hierarchy
2. Test portfolio
3. Differential parity
4. Nonfunctional verification
5. Cutover evidence

## Evidence hierarchy

Prefer, in order:

1. Approved executable results from the authoritative legacy revision.
2. Existing accepted regression tests and interface contracts.
3. Production telemetry and sanitized representative cases.
4. Source/JCL/schema analysis confirmed by an SME.
5. Documentation confirmed current by an owner.
6. Unconfirmed inference, which must remain a hypothesis.

Generated target code and generated target tests cannot validate each other without an independent oracle.

## Test portfolio

Maintain stable test IDs and trace them to business rules and target components:

- Domain unit tests for calculations, decisions, states, and boundary values.
- Component tests for record codecs, SQL mappings, file handling, and adapters.
- Integration tests with production-equivalent databases, messaging, identity, and files.
- Consumer/provider contract tests for APIs, events, and batch file formats.
- Characterization and differential tests against approved legacy outcomes.
- Batch restart, checkpoint, rerun, duplicate, partial failure, and reconciliation tests.
- Transaction, isolation, concurrency, ordering, idempotency, and recovery tests.
- Security tests for authorization, injection, secrets, protected-data exposure, and audit.
- Performance, capacity, soak, batch-window, resilience, failover, backup/restore, and disaster-recovery tests.
- Accessibility and operational acceptance tests.

Use sanitized or synthetic data that retains structural and boundary characteristics. Never copy production protected data into the repository.

## Differential parity

For each scenario capture:

- Input records/request/message and pre-state.
- Legacy output, post-state, messages/files, return/status codes, and relevant side effects.
- Target output and post-state.
- Approved normalization rules for nondeterministic fields.
- Field-by-field comparison, numeric tolerance only where business-approved, ordering rules, and mismatch classification.

Classify each mismatch:

- Defect in target implementation.
- Defect or undefined behavior in legacy requiring a business decision.
- Intentional approved change.
- Test fixture/environment issue.
- Unresolved.

Do not widen tolerances, sort unordered data, replace null with blank, or discard error details merely to make comparisons pass.

## Nonfunctional verification

Measure against explicit thresholds:

- Online latency percentiles, throughput, concurrency, and error rate.
- Batch elapsed time, critical-path window, CPU/memory/I/O, checkpoint cost, and restart time.
- Database/query plans, locks, pool use, log volume, and storage growth.
- Message lag, retries, duplicates, dead letters, and recovery time.
- Availability, RTO/RPO, failover, backup/restore, and rollback.
- Security controls, vulnerabilities, dependency risk, audit completeness, and privacy.
- Observability coverage from entry point through all tiers and external effects.

Use production-like data volumes and topology where feasible. Record environment differences.

## Cutover evidence

Require:

- Approved requirements traceability with no critical unmapped rule.
- Zero unresolved severity-1/2 parity or security defects and an approved disposition for lower severities.
- Reconciled data migration rehearsal and rollback rehearsal.
- Parallel/shadow results over the approved observation period.
- Capacity and batch-window sign-off.
- Security, privacy, architecture, data, operations, support, and business approvals.
- Dashboards, alerts, runbooks, on-call ownership, training, support transition, and hypercare plan.
- Explicit abort thresholds and a named cutover decision authority.

Decommission legacy components only after retention, legal hold, audit, rollback, and dependency-exit criteria are satisfied.
