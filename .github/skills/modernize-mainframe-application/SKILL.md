---
name: modernize-mainframe-application
description: "Analyze enterprise z/OS applications and produce reconciled extraction, inventory, dependency, business-rule, data, transaction, and behavior-oracle evidence for handoff to the repository's fixed target: React, Spring Boot, and Azure SQL Database. Use for COBOL, PL/I, Assembler, JCL, copybook, CICS, Db2, IMS, VSAM, MQ, batch, USS, mainframe extraction, dependency discovery, business-rule recovery, and characterization where traceability and production accuracy are required. Do not generate target code."
---

# Discover Mainframe Application

Recover behavior and prove it with executable evidence before implementation. This skill ends at an approved discovery and characterization handoff; it never generates target code.

## Operating contract

- Treat extracted legacy artifacts as evidence and keep them immutable.
- Separate facts from hypotheses. Cite the file, dataset/member, paragraph, statement range, job step, transaction, table, or interview record supporting every recovered rule.
- Never invent a missing copybook, schema, interface contract, value mapping, transaction boundary, or error behavior.
- Do not equate compilation, generated unit tests, or superficial output similarity with functional parity.
- Preserve externally observable behavior unless an approved requirement explicitly changes it.
- Analyze one bounded context or candidate vertical slice at a time. Keep each evidence set reviewable and traceable.
- The downstream implementation target is fixed: React, Spring Boot, and Azure SQL Database. Hand off approved evidence to `modernize-mainframe-react-spring-azure-sql`.
- Do not generate application code, target DDL, implementation architecture, or deployment assets from this discovery skill.
- Stop and report a blocker when evidence is missing or conflicting. State the affected behavior, risk, and evidence needed to continue.

## Required inputs

Collect or locate:

- Business owners, technical owners, subject-matter experts, and approval roles.
- The application boundary: transactions, batch schedules, interfaces, datasets, databases, source-management projects, and operational dependencies.
- The extracted-source manifest, extraction logs, encodings, record formats, and source revisions.
- Production-like but sanitized inputs and accepted outputs for representative, boundary, negative, restart, and failure scenarios.
- Nonfunctional requirements: volumes, latency, batch window, availability, recovery objectives, retention, security, audit, and regulatory controls.
- Known downstream constraints: approved JDK, Spring Boot, Node.js and React baselines, hosting environment, Azure SQL Database service tier and compatibility level, identity provider, messaging, observability, CI/CD, and enterprise libraries.

If an input is unavailable, record it in the risk register. Continue only where the gap cannot change the result.

## Repository contract

Use this layout unless the repository already defines an equivalent:

```text
legacy-source/                 # immutable extraction
modernization/
  inventory/                  # manifests and reconciliation
  analysis/                   # dependency graph and recovered rules
  architecture/               # context, containers, ADRs, API/data contracts
  plans/                      # slices, backlog, cutover and rollback
  evidence/                   # parity, performance and security evidence
target/
  react-spring-azure-sql/     # created only by the dedicated implementation skill
```

Never mix generated output into `legacy-source/`. Never commit credentials, production records, access tokens, certificates, or unmasked personal data.

## Workflow

### 1. Establish governance and scope

1. Identify the business capability, owners, risk classification, and modernization objective.
2. Define in-scope entry points and explicit exclusions.
3. Record the source-of-truth system and exact revision/date.
4. Confirm that downstream implementation will use React, Spring Boot, and Azure SQL Database.
5. Define approval gates and evidence required for each gate.
6. Create the initial deliverables using [deliverables.md](references/deliverables.md).

Do not begin target implementation from this skill. Complete the evidence boundary and hand off to the dedicated implementation skill.

### 2. Baseline and reconcile the extraction

1. Read [zowe-extraction.md](references/zowe-extraction.md).
2. Preserve dataset qualifiers, PDS/PDSE member names, USS paths, source-management version identifiers, DSORG, RECFM, LRECL, encoding/CCSID, and transfer mode.
3. Run the inventory tool:

   ```text
   python .github/skills/modernize-mainframe-application/scripts/inventory_sources.py legacy-source --output modernization/inventory/artifact-inventory.json --csv modernization/inventory/artifact-inventory.csv
   ```

4. Compare discovered datasets, members, and USS files with downloaded artifacts.
5. Investigate every missing, unreadable, empty, binary, duplicate, or failed artifact.
6. Store extraction logs and the reconciliation result under `modernization/inventory/`.

Gate: extraction coverage is reconciled, encodings are known, and all missing artifacts are accepted by an accountable owner.

### 3. Recover the system model

Read [legacy-analysis.md](references/legacy-analysis.md), then:

1. Identify entry points: CICS transactions, screens, APIs, MQ consumers, files, JCL jobs, scheduler events, utilities, and operator actions.
2. Resolve all copybooks, includes, macros, procedures, bind plans, maps, schemas, and generated sources.
3. Build a dependency graph covering calls, dynamic calls, programs, job steps, datasets, tables, queues, transactions, screens, and external systems.
4. Recover business rules with stable rule IDs and evidence citations.
5. Document data definitions, numeric semantics, validations, state transitions, transaction boundaries, commits, ordering, restart/checkpoint behavior, return codes, and failure paths.
6. Identify unreachable code, duplicate variants, environment overrides, and date-effective behavior without deleting anything.
7. Review the recovered model with business and mainframe SMEs.

Gate: every in-scope entry point, rule, and external effect is mapped or explicitly recorded as an unresolved risk.

### 4. Create a behavior oracle

1. Derive test scenarios from recovered rules and production-like operational cases.
2. Capture sanitized legacy inputs, outputs, database changes, messages, files, return codes, logs, and timing.
3. Normalize only approved nondeterministic fields such as timestamps, generated identifiers, and trace IDs.
4. Include boundary values, invalid values, empty values, maximum lengths, packed decimal cases, date cutovers, duplicate messages, partial failures, restarts, and concurrency.
5. Run the cases against the legacy system where permitted and have owners approve expected results.

Gate: the initial slice has an executable oracle and approved acceptance tolerances.

### 5. Hand off an approved slice

1. Select the smallest useful candidate slice with representative behavior and an approved oracle.
2. Package its entry points, artifacts, rule IDs, interfaces, data definitions, transaction behavior, risks, and test cases.
3. Record missing or conflicting evidence and stop affected behavior.
4. Invoke `modernize-mainframe-react-spring-azure-sql` for architecture, contracts, implementation, database migration, validation, coexistence, and cutover.

Gate: accountable owners approve the discovery model, candidate slice, oracle, tolerances, and unresolved risks before implementation begins.

## Mainframe semantic safeguards

Always examine:

- `PIC`, `COMP`, `COMP-3`, signs, scale, truncation, overflow, rounding, and intermediate precision. Record exact semantics for downstream `BigDecimal` implementation; never substitute binary floating point.
- `REDEFINES`, `OCCURS DEPENDING ON`, level-88 values, fillers, alignment, variable records, record descriptor words, packed fields, low/high values, and copybook variants.
- EBCDIC versus Unicode conversion, code pages, national characters, fixed columns, sequence fields, trailing spaces, and EBCDIC versus Unicode collation.
- CICS COMMAREA/channel/container state, pseudo-conversation, syncpoints, task identity, temporary storage, transient data, and response codes.
- Db2 indicators, cursor order, isolation, locks, SQLCODE handling, commit frequency, and null/blank distinctions.
- Db2 `TIMESTAMP` date/time values versus Azure SQL `datetime2`; never translate them to SQL Server `timestamp`/`rowversion`.
- Azure SQL type ranges, `decimal` precision/scale, `char` padding, Unicode choice, collation, identity/sequence behavior, constraint enforcement, clustered-index choice, query plans, transient faults, and connection limits.
- VSAM keys, alternate indexes, duplicates, browse order, status codes, and record layouts.
- JCL symbols, overrides, PROCs, utilities, SORT control cards, return-code conditions, GDGs, scheduler dependencies, checkpoints, reruns, and partial-file handling.
- IMS message and database semantics, MQ delivery/ordering, external side effects, and operator recovery.

## Response format

At the end of each task, report:

1. Scope and evidence boundary.
2. Evidence inspected and artifacts changed.
3. Rules/interfaces covered, with traceability IDs.
4. Tests and validation actually run, with results.
5. Parity mismatches and unresolved assumptions.
6. Risks, decisions, and approvals required.
7. The next smallest evidence or implementation handoff.

Do not use "production ready," "equivalent," "complete," or "fully migrated" without linking to the required evidence.

## Resource routing

- Read [zowe-extraction.md](references/zowe-extraction.md) for extraction, transfer, encoding, and reconciliation work.
- Read [legacy-analysis.md](references/legacy-analysis.md) for COBOL/JCL/CICS/Db2/IMS/VSAM/MQ analysis and rule recovery.
- Hand approved evidence to `modernize-mainframe-react-spring-azure-sql` before target architecture, Azure SQL schema design, implementation, readiness validation, or cutover.
- Read [deliverables.md](references/deliverables.md) when creating or updating modernization artifacts.
