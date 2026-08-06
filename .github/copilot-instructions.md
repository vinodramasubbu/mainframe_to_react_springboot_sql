# Enterprise mainframe modernization instructions

This repository modernizes a production mainframe application. Correctness, traceability, security, and operational continuity take precedence over speed or code volume.

- Use `modernize-mainframe-application` only for platform-neutral extraction, inventory, dependency analysis, rule recovery, characterization, slice ordering, and the evidence-backed cross-phase roadmap/DAG. It ends at an approved evidence handoff and must not generate target code or target design.
- Use `modernize-mainframe-react-spring-azure-sql` only after that evidence boundary is approved. It refines and executes the approved target nodes in the same roadmap/DAG for architecture, implementation, validation, coexistence, and cutover.
- Treat `legacy-source/` as immutable evidence. Do not format, rename, reorganize, "clean up," or edit extracted files.
- React with TypeScript, Spring Boot, and Azure SQL Database are the only implementation target. Put target code only under `target/react-spring-azure-sql/` and stack-specific analysis only under `modernization/react-spring-azure-sql/`.
- Do not translate the estate in one pass. Work in approved bounded vertical slices with explicit entry points, rules, interfaces, data, tests, and rollback.
- Persist phase and slice dependencies under `modernization/plan/`; execute only the next dependency-satisfied node, record actual results, and never cross an unsatisfied evidence or accountable-owner gate.
- Every recovered business rule must have a stable ID, a legacy evidence citation, a target implementation mapping, and at least one test.
- Never invent missing copybooks, schemas, record layouts, external contracts, value mappings, or failure behavior. Record gaps and stop the affected work.
- Preserve decimal precision, encodings, fixed-record behavior, ordering, null/blank distinctions, transaction boundaries, restart behavior, return codes, security, audit, and external side effects.
- Prefer characterization and differential tests against approved legacy results. Generated tests alone are not proof of parity.
- Keep presentation/API, application/business, and data/integration concerns separated. Domain rules must not depend on HTTP, UI, database, or vendor SDK types.
- Validate input at trust boundaries. Use parameterized data access, least privilege, secrets management, protected-data redaction, dependency scanning, and organization-approved cryptography.
- Pin dependencies and use organization-approved LTS runtimes. Do not introduce libraries, services, database changes, or public contracts without recording the decision and operational consequences.
- Keep changes small, reviewable, reversible, and linked to modernization evidence. Update traceability, risks, ADRs, tests, and runbooks with the code.
- Run the relevant formatter, static analysis, build, tests, security checks, and parity suite. Report commands and actual results; never claim checks were run when they were not.
- Do not place credentials, certificates, tokens, production records, or unmasked personal data in prompts, tests, logs, snapshots, or commits.
- Do not claim "equivalent," "production ready," or "complete" while critical mismatches, missing dependencies, unapproved assumptions, or readiness gates remain.

The current repository may initially contain only extracted artifacts and planning files. Discover actual build commands rather than guessing them, then document stable commands here after the target scaffold is approved.
