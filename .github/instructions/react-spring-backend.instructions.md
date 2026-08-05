---
applyTo: "target/react-spring-azure-sql/backend/**"
---

# Spring Boot backend rules

- Use the approved LTS JDK and pinned Spring Boot/dependency versions.
- Separate API adapters, application orchestration, framework-independent domain rules, and infrastructure adapters.
- Keep domain types independent of Spring, HTTP, ORM, Azure SDK, and generated database types.
- Use `BigDecimal` with explicit precision, scale, rounding, comparison, and overflow behavior. Never use `double` or `float` for business decimals.
- Use explicit domain types for identifiers, money/currency, business dates, timestamps, codes, and statuses.
- Map API DTOs, domain types, and persistence models explicitly. Never expose JPA entities as public contracts.
- Validate transport syntax at boundaries and authoritative business invariants in application/domain code.
- Enforce authenticated identity and policy-based authorization in every use case.
- Define transactions in application services and preserve approved isolation, locks, ordering, commits, rollback, savepoints, restart, and external-side-effect behavior.
- Require idempotency and optimistic concurrency where retries or concurrent updates are possible.
- Choose Spring MVC/WebFlux, JDBC/JPA/jOOQ, and batch technology from evidence and ADRs; do not default to unnecessary complexity.
- Support explicit `local`, `test`, and `azure` database profiles without changing business behavior or SQL. Prefer Testcontainers SQL Server for repeatable database integration tests.
- Never silently substitute H2 or fall back from an Azure configuration to a local database.
- Use centralized safe error mapping, correlation, protected-data redaction, health, metrics, logs, and traces.
- Add domain, API, integration, contract, concurrency, batch/restart, security, performance, and parity tests.
