# Enterprise mainframe modernization instructions

This repository modernizes a production mainframe application. Correctness, traceability, security, and operational continuity take precedence over speed or code volume.

- Use the `modernize-mainframe-react-spring-azure-sql` skill for all planning, architecture, implementation, validation, coexistence, and cutover work. React, Spring Boot, and Azure SQL Database are the repository's only implementation target.
- Use the `modernize-mainframe-application` skill only for platform-neutral extraction, inventory, and legacy discovery. Hand its evidence to the dedicated implementation skill; do not generate target code from the discovery skill.
- Treat `legacy-source/` as immutable evidence. Do not format, rename, reorganize, "clean up," or edit extracted files.
- Do not propose or generate an alternate implementation stack. Put target code only under `target/react-spring-azure-sql/` and stack-specific analysis only under `modernization/react-spring-azure-sql/`.
- Target all converted relational schemas, DDL, migrations, queries, and persistence adapters to Azure SQL Database. Do not use on-premises SQL Server or Azure SQL Managed Instance-only features.
- Do not translate the estate in one pass. Work in approved bounded vertical slices with explicit entry points, rules, interfaces, data, tests, and rollback.
- Every recovered business rule must have a stable ID, a legacy evidence citation, a target implementation mapping, and at least one test.
- Never invent missing copybooks, schemas, record layouts, external contracts, value mappings, or failure behavior. Record gaps and stop the affected work.
- Preserve decimal precision, encodings, fixed-record behavior, ordering, null/blank distinctions, transaction boundaries, restart behavior, return codes, security, audit, and external side effects.
- Treat Db2 `TIMESTAMP` as a date/time value and map it to `datetime2`; never map it to SQL Server `timestamp`, which is `rowversion`. Preserve `DECIMAL(p,s)` exactly unless evidence and a data profile prove a wider target is required.
- Generate Azure SQL DDL in migration files, qualify objects with application schemas, name constraints and indexes deterministically, and make every migration reviewable and repeatable. Never rely on ORM automatic schema creation outside disposable tests.
- Keep schema references consistent across migrations, seeds, and persistence code, and diff the target schema against the full legacy DDL for the whole program group. When a later slice needs columns an earlier reduced-scope slice omitted, add them in a new migration; a missing column, wrong schema qualifier, or narrowed type surfaces only as a generic 5xx, never a clear error.
- Sequence each modernized program as pure business rules first (with characterization values bound as exact expectations), then persistence and orchestration, then the endpoint, then a database integration test; reuse shared legacy subprograms as one dependency-free domain class across online and batch slices.
- Make unexpected-exception handlers log the cause and correlation id; never let a handler silently swallow the failure.
- Document every Db2/VSAM/file-to-Azure-SQL mapping, including defaults, identity behavior, null versus blank, fixed-length padding, Unicode, collation, keys, constraints, indexes, transaction isolation, locking, and sequence/order behavior.
- Prefer characterization and differential tests against approved legacy results. Generated tests alone are not proof of parity.
- Keep presentation/API, application/business, and data/integration concerns separated. Domain rules must not depend on HTTP, UI, database, or vendor SDK types.
- Validate input at trust boundaries. Use parameterized data access, least privilege, secrets management, protected-data redaction, dependency scanning, and organization-approved cryptography.
- Pin dependencies and use organization-approved LTS runtimes. Do not introduce libraries, services, database changes, or public contracts without recording the decision and operational consequences.
- Keep changes small, reviewable, reversible, and linked to modernization evidence. Update traceability, risks, ADRs, tests, and runbooks with the code.
- Run the relevant formatter, static analysis, build, tests, security checks, and parity suite. Report commands and actual results; never claim checks were run when they were not.
- Validate database changes by applying the full migration chain to an empty Azure SQL Database or an approved Azure SQL-compatible test environment, then run schema, data-migration, query-plan, concurrency, rollback, and parity checks.
- Do not place credentials, certificates, tokens, production records, or unmasked personal data in prompts, tests, logs, snapshots, or commits.
- Do not claim "equivalent," "production ready," or "complete" while critical mismatches, missing dependencies, unapproved assumptions, or readiness gates remain.

The current repository may initially contain only extracted artifacts and planning files. Discover actual build commands rather than guessing them, then document stable commands here after the target scaffold is approved.
