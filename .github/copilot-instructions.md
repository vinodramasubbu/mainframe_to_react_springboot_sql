# Mainframe modernization policy

This repository incrementally replaces evidenced mainframe capabilities with React, Spring Boot, and Azure SQL Database. Correctness, traceability, security, and operational continuity take precedence over speed or code volume.

- Treat `legacy-source/` as immutable forensic evidence. Never edit, format, rename, move, regenerate, or repair it in place.
- Separate observed facts, interpretations, assumptions, and unresolved gaps. Never invent missing copybooks, layouts, schemas, contracts, mappings, side effects, or failure behavior.
- Recover and implement one bounded business capability at a time. Each slice must name its entry points, exclusions, rule IDs, interfaces, data, oracle cases, acceptance gates, and rollback.
- Give every recovered rule a stable ID, precise legacy evidence citation, target mapping, and at least one test. Target-derived tests are not independent parity evidence.
- Preserve externally observable precision, rounding, encoding, fixed-width behavior, null versus blank, ordering, transactions, restart, return codes, authorization, audit, and side effects unless an approved requirement changes them.
- Put discovery and planning evidence under `modernization/`. Put target code only under `target/react-spring-azure-sql/`. Do not introduce another implementation stack.
- Keep React presentation, Spring application/domain behavior, and data/integration adapters separate. The backend owns authoritative business rules and transactions; the browser communicates through approved contracts only.
- Target Azure SQL Database. Local SQL Server is an inner-loop test environment, not Azure compatibility evidence.
- Validate trust boundaries, use parameterized data access and least privilege, redact protected data, and never place credentials or production records in source, prompts, fixtures, snapshots, or logs.
- Pin approved dependencies and record consequential contract, schema, infrastructure, security, and operational decisions.
- Keep changes reviewable and reversible. Update traceability, tests, risks, and runbooks with the implementation.
- Report commands and actual pass, fail, skip, and blocked results. Never claim parity, readiness, or completion while critical evidence, approvals, or validation gates are missing.

Use the role-based agents in `.github/agents/` for discovery, planning, implementation, and independent validation. Use path-scoped instructions for technology-specific rules and skills for repeatable specialist procedures.
