---
name: implement-mainframe-slice
description: "Implement one approved, evidence-backed mainframe modernization slice using React and TypeScript, Spring Boot, and Azure SQL Database. Use for domain rules, APIs, migrations, persistence, frontend workflows, local SQL integration, tests, traceability, and implementation repair."
user-invocable: false
---

# Implement Mainframe Slice

Build one approved vertical slice without changing its evidence or scope.

## Entry gate

Require a reviewed plan containing entry points, exclusions, legacy rule citations, oracle cases, UI/API/data contracts, security requirements, acceptance gates, rollback, and unresolved risks. Return to analysis or planning if a gap can alter implementation behavior.

## Procedure

1. Bind approved characterization values into exact domain or contract tests.
2. Implement dependency-free Java domain types and rules. Use `BigDecimal` with explicit scale and rounding for business decimals.
3. Implement application orchestration, authorization, idempotency, concurrency, and explicit transactions.
4. Add Azure SQL-compatible, versioned migrations and parameterized persistence adapters. Keep one migration chain and disable automatic production DDL.
5. Implement the approved OpenAPI operation and safe error contract. Log unexpected causes with a correlation identifier while redacting protected data.
6. Generate or validate TypeScript API types and implement the accessible React task. Do not duplicate authoritative rules in the browser.
7. Add focused domain, API, component, accessibility, database, security, concurrency, and end-to-end tests required by the slice.
8. After each substantive edit, run the cheapest check that can falsify it. Then run the applicable integrated suite.
9. Update traceability, target README, runbooks, risks, and rollback notes with the implementation.
10. Hand the result to an independent Validation Critic. Do not self-certify parity.

## Technical safeguards

- React communicates only through approved contracts and never accesses Azure SQL directly.
- Preserve exact decimal, date/time, fixed-character, null/blank, ordering, version, transaction, restart, and return-code behavior.
- Map Db2 `TIMESTAMP` date/time values to reviewed `datetime2` semantics, never SQL Server `timestamp`/`rowversion`.
- Local SQL Server is an inner-loop environment; it is not Azure SQL compatibility evidence.
- Unexpected failures must preserve the public error contract and retain diagnosable server-side cause and correlation data.

Use the implementation references when needed: [React](./references/react-frontend.md), [Spring and Azure SQL](./references/spring-azure-sql.md), and [local SQL testing](./references/local-sql-testing.md).
