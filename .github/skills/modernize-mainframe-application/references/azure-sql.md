# Azure SQL Database target

Use Azure SQL Database as the converted relational target. Do not assume Azure SQL Managed Instance or on-premises SQL Server compatibility.

## Required design sequence

1. Inventory every source table, view, index, key, constraint, default, sequence/identity, trigger, stored procedure, cursor, isolation clause, SQLCODE branch, and data access path.
2. Profile sanitized source data for actual lengths, numeric ranges, nulls, blanks, invalid codes, duplicates, and referential gaps.
3. Create `modernization/architecture/database/source-to-target-map.csv` before generating DDL.
4. Record unresolved semantic choices in the risk register; never guess.
5. Generate versioned, reviewable migrations for the selected application stack.
6. Apply the entire migration chain to an empty Azure SQL Database or approved compatibility environment.
7. Load representative data and verify counts, totals, hashes where appropriate, keys, constraints, query results, ordering, concurrency, and parity.

## Type mapping guardrails

Use evidence and the following defaults:

| Legacy meaning | Azure SQL default | Required check |
|---|---|---|
| `SMALLINT` | `smallint` | Confirm signed range |
| `INTEGER` | `int` | Confirm signed range |
| `BIGINT` | `bigint` | Confirm signed range |
| `DECIMAL(p,s)` / packed decimal | `decimal(p,s)` | Preserve precision, scale, sign, rounding, overflow |
| `CHAR(n)` code or fixed identifier | `char(n)` or `varchar(n)` by evidence | Test trailing-space comparison and padding |
| `VARCHAR(n)` non-Unicode | `varchar(n)` | Verify encoding and character repertoire |
| Names or international text | `nvarchar(n)` | Verify that source byte length is not mistaken for character length |
| `DATE` | `date` | Verify accepted range and invalid legacy dates |
| Db2 `TIMESTAMP` | `datetime2(6)` unless source precision proves another scale | Never use SQL Server `timestamp`; it means `rowversion` |
| Generated numeric identity | `bigint IDENTITY(1,1)` or approved sequence | Verify reseed, load, and replay behavior |
| Binary fields | `varbinary(n)` | Preserve bytes; do not transcode |

Do not use `money`, `smallmoney`, `float`, or `real` for business amounts. Do not silently coerce blank strings to `NULL`, zero dates to dates, or EBCDIC sentinel values to Unicode characters.

## DDL rules

- Use T-SQL accepted by Azure SQL Database.
- Use application schemas such as `[trsy]` and `[bank]`; do not use `dbo` implicitly.
- Quote identifiers with brackets only when required or applied consistently.
- Name primary keys, foreign keys, unique constraints, checks, defaults, and indexes deterministically.
- Translate Db2 `WITH DEFAULT` into explicit Azure SQL `DEFAULT` constraints.
- Use `SYSUTCDATETIME()` for new UTC audit timestamps only when UTC is an approved behavior change. Otherwise preserve the documented legacy time basis.
- Preserve natural and composite keys. Introduce surrogate keys only through an ADR with migration and traceability consequences.
- Choose clustered indexes deliberately; a primary key does not automatically prove the correct physical order.
- Create supporting indexes from evidenced access paths and validate them with representative query plans and volume.
- Do not encode business rules only in filtered indexes, triggers, computed columns, or database-specific behavior unless the rule catalog and ADR require it.
- Do not put `GO` inside migrations executed through JDBC, EF Core, or other APIs unless the migration tool explicitly parses it.
- Do not use unsupported or excluded capabilities such as SQL Server Agent, linked servers, `xp_cmdshell`, CLR integration, filesystem access, or cross-database transactions.

## Semantics requiring explicit decisions

Document and test:

- Database and column collation, including case, accent, width, kana, and binary ordering where relevant.
- EBCDIC versus Unicode sorting and fixed-width trailing-space behavior.
- Db2 isolation (`UR`, `CS`, `RS`, `RR`), update-lock intent, cursor stability, lock duration, deadlocks, and timeout handling versus Azure SQL isolation.
- Whether `READ_COMMITTED_SNAPSHOT` and/or snapshot isolation is enabled.
- Optimistic concurrency tokens. Preserve a legacy numeric `VERSION_NO` when externally observable; do not silently replace it with `rowversion`.
- Identity value retrieval, sequence gaps, reruns, idempotency, duplicate handling, and deterministic ordering.
- Savepoint, partial-commit, restart/checkpoint, and rollback behavior.
- Constraint timing differences and the order used to load parent/child data.
- Date/time zone, precision, default, and comparison semantics.
- String truncation, implicit conversion, arithmetic overflow, and divide-by-zero behavior.

## Security and operations

- Prefer Microsoft Entra workload identity or managed identity for deployed applications.
- Grant the application only required schema/object permissions; use a separate deployment identity for migrations.
- Store fallback secrets in an approved secret store and never in source, migrations, logs, or test fixtures.
- Require encrypted connections and validate server certificates.
- Define auditing, vulnerability assessment, threat protection, data classification, masking policy where appropriate, retention, backup, restore, geo-recovery, and reconciliation ownership.
- Size the service tier from measured workload evidence. Validate connection pool limits, transient-fault handling, throttling, storage growth, log rate, and batch window.

## Migration verification gate

Do not approve the database slice until evidence shows:

- The full migration chain succeeds from an empty database and from every supported prior version.
- The schema contains the intended objects, types, nullability, defaults, keys, constraints, and indexes.
- Data migration is restartable or safely repeatable and reconciles row counts, control totals, rejected rows, and sampled field values.
- Application queries return approved results under representative collation, concurrency, isolation, and volume.
- Rollback or forward-recovery has been rehearsed without losing accepted transactions.
- No Azure SQL Managed Instance-only or on-premises SQL Server-only dependency remains.
