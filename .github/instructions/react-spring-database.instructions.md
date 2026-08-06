---
description: "Use for Azure SQL Database schema design, source-to-target mapping, T-SQL, migrations, persistence queries, data migration, compatibility, or database review."
applyTo: "target/react-spring-azure-sql/database/**,target/react-spring-azure-sql/backend/src/main/resources/db/migration/**"
---

# Azure SQL Database rules

- Generate Azure SQL Database-compatible T-SQL. Do not depend on Azure SQL Managed Instance or on-premises SQL Server-only features.
- Create and approve the source-to-target data map before writing DDL.
- Preserve `DECIMAL(p,s)` exactly unless approved profiling proves a wider type is required. Do not use `money`, `smallmoney`, `float`, or `real` for business values.
- Map Db2 `TIMESTAMP` date/time values to approved `datetime2` semantics, never SQL Server `timestamp`/`rowversion`.
- Decide Unicode, collation, fixed-character padding, null versus blank, default, identity, natural/composite keys, clustered indexes, isolation, locking, ordering, and UTC behavior explicitly.
- Preserve externally observable numeric version tokens; do not silently replace them with `rowversion`.
- Use named application schemas, constraints, defaults, keys, and indexes.
- Keep one authoritative, versioned Flyway or Liquibase migration chain. Disable ORM automatic production DDL.
- Run that same migration chain against a clean local SQL Server for inner-loop testing when enabled; do not maintain a separate local schema.
- Exclude SQL Server Agent, linked servers, `xp_cmdshell`, CLR, filesystem access, and cross-database transactions.
- Use parameterized access, encrypted connections, certificate validation, least-privilege migration/application identities, and Microsoft Entra managed identity where approved.
- Apply the entire migration chain to an empty approved database, then verify metadata, data reconciliation, constraints, indexes, plans, concurrency, performance, and recovery.
- Treat local SQL Server results as preliminary. Repeat the required compatibility and readiness checks against Azure SQL Database.
