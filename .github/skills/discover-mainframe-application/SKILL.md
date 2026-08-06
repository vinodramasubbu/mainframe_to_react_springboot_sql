---
name: discover-mainframe-application
description: "Recover and document an enterprise mainframe application's evidenced behavior from COBOL, copybooks, CICS, BMS, JCL, Db2, VSAM, files, and scheduler definitions. Use for source inventory, dependency analysis, business rules, data semantics, transaction behavior, characterization, and project README creation. Do not generate target code."
user-invocable: false
---

# Discover Mainframe Application

Produce an approved evidence package before target planning or implementation.

## Required procedure

1. Define the application boundary, owners, source revision, entry points, and explicit exclusions.
2. Reconcile extracted artifacts, encodings, fixed-record metadata, missing dependencies, and duplicates. Use [inventory_sources.py](./scripts/inventory_sources.py) when a deterministic local inventory is needed.
3. Resolve calls, copybooks, maps, transactions, job steps, datasets, tables, files, queues, and external interfaces into a dependency model.
4. Recover business rules and observable behavior with stable IDs and precise legacy citations.
5. Record data types, precision, scale, signs, padding, encoding, null/blank behavior, allowed values, sensitivity, and keys.
6. Recover authorization, commits, rollback, ordering, restart, return codes, failures, audit, and external side effects.
7. Capture sanitized independent legacy inputs and outcomes as characterization cases when access and approval permit. Do not manufacture an oracle from target code.
8. Review gaps and conflicting evidence. Stop only the affected behavior and state what verified evidence is needed.
9. Create or refresh the project README from approved findings. Include purpose, architecture context, capabilities, repository map, prerequisites, setup, operation, testing, status, limitations, security, and contribution guidance where evidenced and relevant.

## Outputs

Create outputs under `modernization/inventory/`, `modernization/analysis/`, and `modernization/evidence/characterization/` as they become relevant. These files are the durable handoff to planning; they are not bundled example checkpoints. At minimum produce:

- system context and entry-point inventory;
- dependency graph;
- business-rule catalog;
- data dictionary;
- transaction, failure, and restart model;
- interface and user-task inventory;
- characterization case index;
- unknowns and risk register.

Every conclusion must distinguish observed fact from interpretation or assumption. Never modify `legacy-source/` or create target architecture and code.

Load [z/OS extraction guidance](./references/zowe-extraction.md) for extraction and reconciliation work and [legacy analysis guidance](./references/legacy-analysis.md) for COBOL, CICS, JCL, Db2, VSAM, IMS, MQ, and fixed-record semantics.
