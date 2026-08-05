# Modernization deliverables

Create only artifacts relevant to the current phase, using these canonical locations.

## Inventory

`modernization/inventory/`

- `extraction-manifest.csv`: original system/revision, dataset/member or USS path, local path, attributes, encodings, transfer mode, hash, status, exclusion approval.
- `artifact-inventory.json` and `.csv`: deterministic local inventory from the bundled script.
- `reconciliation.md`: expected, extracted, excluded, failed, and resolved counts with owner approval.

## Analysis

`modernization/analysis/`

- `system-context.md`: actors, capabilities, entry points, external systems, trust boundaries.
- `dependency-graph.mmd`: stable node/edge IDs and evidence.
- `entry-points.csv`: trigger, contract/layout, program/job, authorization, outcome.
- `business-rules.csv`: rule ID, trigger, inputs, rule, effects, exceptions, transaction, evidence, confidence, owner.
- `data-dictionary.csv`: field, copybook/schema source, type, precision/scale, encoding, allowed values, sensitivity, target type.
- `unknowns-and-risks.csv`: ID, gap, affected scope, consequence, evidence needed, owner, due date, status.

## Architecture

`modernization/architecture/`

- `target-architecture.md`: tier/module/deployment boundaries and runtime views.
- `database/source-to-target-map.csv`: legacy object/field, evidence, Azure SQL object/column, type, null/default conversion, key/index mapping, transformation, and validation.
- `database/azure-sql-schema.sql`: reviewable Azure SQL Database-compatible baseline DDL when a baseline script is part of the approved migration strategy.
- `database/migration-plan.md`: load order, transformations, restartability, reconciliation, performance, security, rollback or forward recovery, and ownership.
- `database/schema-verification.md`: clean deployment, migration chain, constraints, indexes, query plans, concurrency, and compatibility results.
- `contracts/`: OpenAPI, event schemas, file layouts, and compatibility rules.
- `threat-model.md`: assets, trust boundaries, threats, controls, residual risks.
- `adrs/ADR-NNNN-title.md`: context, decision, alternatives, evidence, consequences, owner, status, date.

Minimum ADRs cover target platform, Azure SQL physical design and compatibility level, deployment structure, persistence, transactions, integration, batch, identity, observability, hosting, and coexistence.

## Planning and traceability

`modernization/plans/`

- `requirements-traceability.csv`: rule/interface ID, legacy evidence, target component, test IDs, result, owner.
- `migration-backlog.csv`: slice, capability, entry point, dependencies, risk, evidence readiness, status.
- `cutover.md`: steps, owners, timing, prerequisites, reconciliation, abort thresholds, rollback, communications.
- `decommission.md`: dependencies, retention, legal hold, data disposition, access removal, support exit, approval.

## Evidence

`modernization/evidence/`

- `characterization/`: approved legacy cases and provenance without protected data.
- `parity/`: comparison reports and mismatch disposition.
- `performance/`, `security/`, `resilience/`, and `operations/`: commands, environments, results, thresholds, and approvals.

## Work-packet template

Every implementation slice or pull request should state:

```text
Capability/slice:
Entry points:
Legacy artifacts and revision:
Business-rule IDs:
Interfaces and data:
Intentional behavior changes:
Target components:
Test/oracle IDs:
Commands executed and results:
Parity mismatches:
Security/operations impact:
Rollback:
Risks/assumptions:
Approvals:
```

## ADR template

```text
# ADR-NNNN: Decision
Status:
Date:
Owners:

## Context and evidence
## Decision
## Alternatives considered
## Consequences and risks
## Validation and rollback
## Traceability
```
