# How to modernize with GitHub Copilot

This repository uses role-based GitHub Copilot agents, focused skills, path instructions, and one deterministic hook to modernize one evidenced business capability at a time.

## Customization model

| Primitive | Purpose in this repository |
|---|---|
| Repository instructions | Non-negotiable evidence, traceability, security, and lifecycle policy |
| Path instructions | Rules for legacy evidence, modernization artifacts, frontend, backend, database, local SQL, or E2E files |
| Custom agents | Separate responsibilities, tools, context, and lifecycle authority |
| Skills | Repeatable specialist procedures loaded by the relevant agent |
| Handoffs | Explicit transitions from discovery to planning to implementation to validation |
| Hooks | Deterministic enforcement that instructions alone cannot guarantee |

Prompt files are not required. Select an agent directly or start with the Modernization Orchestrator.

## Clean starting state

Before a test run, `modernization/` and `target/` should each contain only their contract README. Discovery creates `modernization/inventory/`, `modernization/analysis/`, and characterization evidence. Planning adds `modernization/architecture/` and `modernization/plans/`. Implementation creates `target/react-spring-azure-sql/`. Validation adds results under `modernization/evidence/`.

Commit or otherwise preserve a completed run before resetting these directories. They contain traceability and approval evidence, not disposable build output.

## 1. Discover and document

Select **Legacy Analyst** and identify an application boundary:

```text
Analyze SURVDEMO as immutable legacy evidence. Recover and document its entry
points, dependencies, business rules, data semantics, user tasks, transactions,
failure and restart behavior, interfaces, characterization coverage, and risks.
Refresh the project documentation from approved evidence. Do not design or
implement target code.
```

The analyst uses `discover-mainframe-application`. Expected outputs include a system context, entry-point inventory, dependency graph, business-rule catalog, data dictionary, transaction and failure model, characterization index, and risk register.

Discovery is blocked only where missing or conflicting evidence can change the result. A polished README does not replace the underlying evidence artifacts.

## 2. Plan the MVP and roadmap

Use the analyst's **Plan the first MVP** handoff or select **Modernization Planner**:

```text
Using the approved SURVDEMO discovery evidence, plan the smallest useful
end-to-end MVP in React, Spring Boot, and Azure SQL. Define later slices for the
remaining behavior. Do not modify legacy evidence or target code.
```

The planner uses `plan-mainframe-modernization` and must define:

- a useful business capability, actors, entry points, and exclusions;
- rule, interface, data, and independent oracle IDs;
- React tasks and states, OpenAPI operations and errors, Spring use cases, and Azure SQL mappings;
- authorization, transactions, concurrency, restart, audit, and operational requirements;
- planned tests, measurable acceptance gates, rollback, risks, decisions, and owners;
- a dependency-ordered roadmap for later online, batch, integration, and cutover slices.

Implementation does not begin until critical contracts and evidence gaps are resolved or explicitly approved by accountable owners.

## 3. Implement one approved slice

Use the planner's **Implement the approved slice** handoff or select **Implementation Agent** with the approved task ID.

The implementation agent uses `implement-mainframe-slice` and works in this order:

1. Bind approved characterization outcomes into exact tests.
2. Implement framework-independent Java domain rules.
3. Add application orchestration, authorization, concurrency, and transactions.
4. Add reviewed Azure SQL migrations and parameterized persistence.
5. Implement the approved API and safe error behavior.
6. Implement the accessible React task from the approved contract.
7. Add the required focused and integrated tests.
8. Update traceability, target documentation, runbooks, risks, and rollback notes.

After each substantive edit, run the cheapest check capable of falsifying it. Do not broaden the slice to resolve unrelated behavior.

## 4. Validate independently

Use the implementation agent's **Run independent validation** handoff or select **Validation Critic**.

The critic has read, search, and execute tools but no edit tool. It uses `validate-mainframe-modernization` to:

- verify bidirectional rule and interface traceability;
- criticize target-derived tests, weak assertions, mocks, and missing boundaries;
- run applicable domain, API, frontend, accessibility, database, E2E, security, concurrency, performance, restart, and resilience checks;
- compare target outputs and persisted state with approved legacy outcomes;
- classify every gate as passed, failed, skipped, or blocked;
- report findings by severity and give the narrowest evidence-supported verdict.

The critic does not fix its own findings. Repairs return to the Implementation Agent and are independently revalidated.

## 5. Repeat or prepare cutover

After a slice passes its required gates, return to the roadmap and select the next dependency-ready capability. Coexistence, data migration, parallel run, reconciliation, rollback rehearsal, and decommissioning are planned and validated as explicit slices or readiness gates, not assumed from feature completion.

## Hook behavior

`.github/hooks/protect-legacy-source.json` runs before tool use. Its Python policy script allows reads of `legacy-source/` but denies recognized mutation tools when their request references that directory.

The hook supplements, rather than replaces, source-control review and filesystem permissions. Its focused test is:

```powershell
$read = '{"tool_name":"read_file","input":{"filePath":"legacy-source/X.cbl"}}' |
  python .github/hooks/protect-legacy-source.py
$write = '{"tool_name":"apply_patch","input":{"path":"legacy-source/X.cbl"}}' |
  python .github/hooks/protect-legacy-source.py
```

The read emits no decision; the write emits a `deny` decision.

## Validation layers

| Layer | Evidence produced |
|---|---|
| Domain and unit | Exact recovered rule behavior and boundaries |
| API and contract | Transport validation, authorization, DTOs, and errors |
| Frontend and accessibility | User-task states, keyboard behavior, semantics, and announcements |
| Database integration | Real migration, SQL, mappings, constraints, and transactions |
| Full-stack E2E | Browser-to-API-to-database behavior |
| Differential parity | Target outcomes and persisted state compared with approved legacy outcomes |
| Azure readiness | Azure SQL compatibility, security, plans, concurrency, recovery, and operations |

Passing one layer does not imply another. Report exact commands and pass, fail, skip, and blocked results.

## Definition of done for a slice

- Every in-scope rule and interface has evidence, a target mapping, and a test.
- Approved oracle outcomes remain exact and unchanged.
- Contracts, migrations, seeds, queries, and generated clients agree.
- Precision, encoding, fixed-width, null/blank, ordering, transaction, failure, and restart behavior are verified where applicable.
- Security, accessibility, operations, reconciliation, and rollback gates have explicit results.
- Independent validation has no unresolved critical mismatch.
- The readiness claim uses only the narrowest status supported by evidence.
