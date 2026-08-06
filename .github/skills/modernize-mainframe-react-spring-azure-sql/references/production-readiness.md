# Verification and production-readiness gates

## Evidence hierarchy

Prefer approved executable legacy results, accepted regression tests/contracts, production telemetry with sanitized cases, source analysis confirmed by an SME, and current owner-confirmed documentation, in that order. Unconfirmed inference remains a hypothesis. Generated target code and generated target tests cannot validate each other without an independent oracle.

## Differential parity

For every scenario record input and pre-state, legacy output/post-state/side effects, target output/post-state, approved normalization, field-level comparison, ordering rules, mismatch classification, and disposition.

Classify each mismatch as a target defect, undefined or defective legacy behavior needing a decision, intentional approved change, fixture/environment issue, or unresolved. Never widen tolerances, sort unordered data, replace null with blank, or discard errors merely to pass.

## Nonfunctional evidence

Measure explicit thresholds for online latency/throughput/errors, batch windows and restart, database plans/locks/pools/growth, messaging lag/retries/duplicates, availability and RTO/RPO, failover and backup/restore, security and audit, accessibility, observability, and operational support. Use production-like volume and topology where feasible and record environment differences.

## Cutover gate

Require:

- Approved traceability with no critical unmapped behavior.
- No unresolved severity 1 or 2 parity/security defects and approved lower-severity disposition.
- Reconciled migration, recovery, and rollback rehearsals.
- Approved parallel/shadow evidence, capacity, batch-window, security, privacy, architecture, data, operations, support, and business sign-off.
- Dashboards, alerts, runbooks, on-call ownership, training, support transition, hypercare, abort thresholds, and named decision authority.

Decommission only after retention, legal hold, audit, rollback, and dependency-exit criteria are satisfied.