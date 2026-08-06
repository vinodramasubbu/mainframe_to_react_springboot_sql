# Azure SQL Database target

Use Azure SQL Database as the converted relational target. Do not assume Azure SQL Managed Instance or on-premises SQL Server compatibility.

## Design sequence

1. Inventory source tables, views, indexes, keys, constraints, defaults, identities/sequences, triggers, routines, cursors, isolation clauses, SQLCODE branches, and access paths from approved evidence.
2. Profile sanitized data for lengths, ranges, nulls, blanks, codes, duplicates, and referential gaps.
3. Create `modernization/react-spring-azure-sql/architecture/database/source-to-target-map.csv` before DDL.
4. Record unresolved semantics as blockers; never guess.
5. Generate one versioned, reviewable migration chain.
6. Apply the full chain to an empty approved database and verify representative data, constraints, queries, ordering, concurrency, recovery, and parity.

## Type guardrails

- Preserve `DECIMAL(p,s)` and packed-decimal precision, scale, sign, rounding, and overflow. Do not use `money`, `smallmoney`, `float`, or `real` for business values.
- Map Db2 `TIMESTAMP` date/time values to approved `datetime2` precision, never SQL Server `timestamp`/`rowversion`.
- Choose `char`, `varchar`, and `nvarchar` from evidence and test padding, trailing-space comparison, source byte versus character length, encoding, and collation.
- Preserve natural/composite keys and observable numeric version tokens. Add surrogate keys or `rowversion` only through an approved behavior change and ADR.
- Preserve binary fields as bytes and never silently coerce blanks to `NULL`, invalid legacy dates, or EBCDIC sentinels.

## DDL and semantic decisions

- Use Azure SQL-compatible T-SQL, named application schemas, and deterministic names for constraints, defaults, keys, and indexes.
- Keep one Flyway or Liquibase chain and disable ORM production DDL.
- Decide collation, Unicode, fixed-width padding, null/blank, defaults, UTC/time zones, identity gaps, ordering, clustered indexes, isolation, locking, savepoints, partial commits, restart, idempotency, and constraint timing explicitly.
- Validate indexes and query plans against evidenced access paths and representative volume.
- Exclude SQL Server Agent, linked servers, `xp_cmdshell`, CLR, filesystem access, and cross-database transactions.

## Security and readiness gate

- Prefer Microsoft Entra workload or managed identity, separate migration/application principals, least privilege, encrypted connections, and certificate validation.
- Define auditing, data classification, threat protection, retention, backup/restore, geo-recovery, reconciliation, transient-fault handling, and connection limits.
- Require clean and supported upgrade migrations, exact metadata verification, restartable data migration, row/control-total reconciliation, representative query and concurrency results, and rehearsed rollback or forward recovery.
- Local SQL Server evidence is preliminary; final compatibility and readiness evidence must run against Azure SQL Database.