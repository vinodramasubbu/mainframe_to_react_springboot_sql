---
applyTo: "target/react-spring-azure-sql/**,modernization/react-spring-azure-sql/**"
---

# React, Spring Boot, and Azure SQL modernization rules

- Use the `modernize-mainframe-react-spring-azure-sql` skill.
- Work only in an approved vertical slice with named legacy evidence, rule IDs, task/screen IDs, interface IDs, oracle cases, rollback, and acceptance gates.
- Keep `legacy-source/` immutable and put stack-specific analysis under `modernization/react-spring-azure-sql/`.
- Keep the tiers independent: React calls approved APIs; Spring Boot owns authoritative use cases and domain rules; Azure SQL is accessed only through backend data adapters.
- Do not implement the same business calculation, validation rule, authorization decision, or state transition independently in React and Spring.
- Define and approve OpenAPI, error, identity, and database contracts before parallel frontend/backend implementation.
- Use exact decimal strings at the JSON boundary where needed to prevent JavaScript precision loss. Use `BigDecimal` in Java and exact `decimal(p,s)` in Azure SQL.
- Preserve approved null/blank, date/time, ordering, concurrency, transaction, restart, failure, audit, and external-side-effect behavior.
- Keep every change traceable from legacy evidence through target component and test result.
- Ensure `target/react-spring-azure-sql/README.md` exists, and update it in the same change whenever target functionality, prerequisites, environment variables, ports, startup steps, migrations, seed data, API or security behavior, tests, limitations, or cleanup commands are added or changed. Keep its commands executable, distinguish sample/local evidence from Azure validation, and never include secrets.
- Run relevant build, static analysis, unit, component, contract, integration, accessibility, security, end-to-end, parity, migration, and performance checks. Report actual results.
- Never claim equivalence or readiness while critical mismatches, missing dependencies, unapproved assumptions, or failed gates remain.
