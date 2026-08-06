# Spring Boot and Azure SQL rules

Implement authoritative use cases in Spring Boot and persist approved state in Azure SQL Database.

## Backend structure

Prefer modules or packages that make dependencies explicit:

```text
backend/
  domain/                  value types, policies, state transitions
  application/             use cases, ports, transactions
  api/                     HTTP contracts and boundary mapping
  infrastructure/          Azure SQL, messaging, files, mainframe adapters
```

Domain code must not depend on Spring, HTTP, ORM, Azure SDK, or generated database types.

## Java and API semantics

- Use `BigDecimal` with explicit scale, rounding, overflow, and comparison rules for decimal business values. Never use `double` or `float`.
- Use explicit types for identifiers, money/currency, business dates, timestamps, codes, and states.
- Map ISO date/time contracts deliberately to `LocalDate`, `Instant`, `OffsetDateTime`, or another approved type.
- Validate transport syntax at the API boundary and business invariants in the application/domain tiers.
- Map DTOs explicitly. Never expose persistence entities as public contracts.
- Use stable error codes and an approved problem response. Keep internal exceptions and SQL details private.
- Enforce authenticated identity and policy-based authorization for every use case.
- Require idempotency for retryable commands and version checks for concurrent updates where legacy behavior requires them.
- Define transaction boundaries in application services and keep external side effects consistent with the approved outbox, orchestration, or recovery design.

Choose Spring MVC/WebFlux, Spring Data JDBC/JPA, jOOQ, or direct JDBC from evidence and an ADR. Do not use reactive complexity around blocking dependencies or default to ORM when SQL control is material.

## Batch and integration

- Use Spring Batch only when its job identity, parameters, checkpoints, commit intervals, skip/retry, restart, and failure semantics match the approved legacy model.
- Preserve fixed-record layouts, encodings, ordering, header/trailer totals, reject files, return-code outcomes, and partial-file handling.
- Make MQ/event delivery, deduplication, ordering, redelivery, and external side effects explicit.
- Keep CICS/Db2/VSAM/IMS coexistence behind ports and adapters.

## Azure SQL type rules

Use evidence and these defaults:

| Legacy meaning | Azure SQL target |
|---|---|
| `SMALLINT` | `smallint` |
| `INTEGER` | `int` |
| `BIGINT` | `bigint` |
| `DECIMAL(p,s)` or packed decimal | `decimal(p,s)` |
| Db2 `DATE` | `date` |
| Db2 `TIMESTAMP` date/time | `datetime2(6)` unless source precision proves another scale |
| Fixed code/identifier | `char(n)` or `varchar(n)` after padding analysis |
| International names/text | `nvarchar(n)` after repertoire analysis |
| Generated numeric identity | approved `IDENTITY` or sequence |
| Binary data | `varbinary(n)` |

Never use `money`, `smallmoney`, `float`, or `real` for business amounts. SQL Server `timestamp` means `rowversion`; never use it for a Db2 date/time value.

## Schema and migration rules

- Generate Azure SQL Database-compatible T-SQL only.
- Use named application schemas, constraints, defaults, keys, and indexes.
- Preserve natural/composite keys unless an ADR approves a change.
- Preserve a legacy numeric version token when observable; do not silently replace it with `rowversion`.
- Decide collation, Unicode, trailing-space comparison, fixed padding, null/blank, identity, clustered indexes, snapshot settings, lock behavior, query ordering, and UTC policy explicitly.
- Translate Db2 isolation and update-lock intent only after concurrency analysis.
- Own production schemas through reviewed Flyway or Liquibase migrations. Disable ORM automatic DDL outside disposable tests.
- Keep one authoritative migration chain under `backend/src/main/resources/db/migration/` or another approved single location.
- Do not place API-executed `GO` separators in migrations unless the migration tool explicitly parses them.
- Exclude SQL Server Agent, linked servers, `xp_cmdshell`, CLR, filesystem access, cross-database transactions, and Managed Instance-only features.

## Connectivity and operations

- Use the approved Microsoft SQL Server JDBC driver.
- Support a `local`/`test` SQL Server profile using the same driver, SQL, mappings, and migrations. Prefer Testcontainers for repeatable automated database tests.
- Do not use H2 or another database dialect as Azure SQL parity evidence.
- Prefer Microsoft Entra workload or managed identity in deployed environments.
- Use a separate least-privilege migration identity and application identity.
- Keep fallback secrets in an approved secret store.
- Require encryption and certificate validation.
- Restrict any local self-signed-certificate trust exception to the local/test profile; never enable it in Azure or shared environments.
- Bound connection pools and retries. Never repeat a non-idempotent transaction blindly after a transient error.
- Measure query plans, locks, deadlocks, throttling, log rate, storage growth, pool saturation, and batch windows at representative volume.
- Define auditing, classification, retention, backup, restore, geo-recovery, RTO, RPO, and reconciliation ownership.

## Database gate

Require evidence that:

- Local SQL Server integration tests pass from a clean disposable database when local testing is enabled.
- The full migration chain succeeds from an empty database and every supported prior version.
- Metadata matches approved types, nullability, defaults, keys, constraints, and indexes.
- Data migration is restartable or safely repeatable and reconciles counts, totals, rejects, and sampled values.
- Application queries meet approved parity, concurrency, ordering, and performance criteria.
- Rollback or forward recovery is rehearsed without losing accepted work.
- The same migrations and required tests pass against Azure SQL Database; local SQL Server success alone is insufficient.
