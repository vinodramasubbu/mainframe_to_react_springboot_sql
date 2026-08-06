---
name: plan-mainframe-modernization
description: "Plan an incremental mainframe migration from approved business evidence to React, Spring Boot, and Azure SQL Database. Use for MVP selection, vertical slices, roadmaps, OpenAPI and data contracts, acceptance gates, coexistence, rollback, and cutover planning. Do not implement target code."
user-invocable: false
---

# Plan Mainframe Modernization

Turn approved discovery evidence into an incremental, testable delivery plan.

## Entry gate

Require an identified application revision, reconciled in-scope artifacts, entry points, rule catalog, data semantics, dependencies, risks, and available independent oracle cases. Do not convert unresolved behavior into a design assumption.

## Procedure

1. Inventory candidate user and batch capabilities with their business value, dependencies, risk, and evidence readiness.
2. Select the smallest useful end-to-end MVP that exercises a real entry point, meaningful business rules, backend behavior, and persistence where the capability requires it.
3. Define the MVP's actors, entry points, rules, interfaces, data, security, operations, exclusions, intentional changes, and dependencies.
4. Map legacy tasks to React routes and states, public operations and errors to OpenAPI, domain behavior to Spring use cases, and legacy data to Azure SQL.
5. Decide transaction, concurrency, idempotency, restart, audit, observability, coexistence, reconciliation, and rollback behavior from evidence or explicit approved decisions.
6. Map each rule and interface to an oracle case, target component, planned test, and acceptance gate.
7. Sequence later slices by business value and dependency order to cover remaining online, batch, integration, operational, and cutover behavior.
8. Record decisions as ADRs, unresolved gaps as owned risks, and approvals as gates. Do not mark a gate approved on an agent's authority.

## Required plan

Each slice must state:

- capability, actors, entry points, and exclusions;
- legacy artifacts, revision, rule IDs, interface IDs, and oracle IDs;
- React, API, Spring, Azure SQL, security, and operational mappings;
- dependencies and explicit non-goals;
- tests and measurable acceptance gates;
- data migration or coexistence needs;
- rollout, reconciliation, rollback, risks, decisions, and owners.

Write architecture and contracts under `modernization/architecture/` and roadmaps, slice plans, and traceability under `modernization/plans/`. Do not modify `legacy-source/` or `target/`.

Load [target architecture guidance](./references/target-architecture.md), [Azure SQL design guidance](./references/azure-sql.md), and [legacy UI recovery guidance](./references/legacy-ui-recovery.md) when those concerns are in scope.
