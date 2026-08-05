# Local SQL Server testing

Use a local SQL Server engine to shorten the development feedback loop while keeping Azure SQL Database as the deployment and compatibility target.

## Test hierarchy

Use:

1. **Testcontainers SQL Server** for repeatable Spring integration and repository tests when an approved container runtime is available.
2. **Docker Compose or another approved local container setup** for interactive development and full-stack testing.
3. **An installed SQL Server Developer or Express instance** when containers are unavailable and organizational policy permits it.
4. **An Azure SQL local development container plus a SQL Database Project** when that toolchain is approved.
5. **An ephemeral Azure SQL Database environment** for the authoritative compatibility, security, performance, and readiness gates.

Do not use H2, HSQLDB, Derby, SQLite, or an unrelated database engine as proof that Azure SQL queries, migrations, locking, collation, or transactions work.

## One schema and one SQL path

- Use the same Microsoft SQL Server JDBC driver, persistence mappings, T-SQL, and Flyway/Liquibase migration chain locally and in Azure.
- Do not create local-only tables, columns, stored procedures, syntax branches, migrations, or relaxed constraints.
- Keep the authoritative migration chain in its approved production location.
- Have the local harness create an empty database, then let the normal migration tool create application schemas and objects.
- Do not put server provisioning, `CREATE DATABASE`, logins, filesystem options, or container administration inside application migrations.
- Reset local integration tests from an empty disposable database or container. Do not depend on a developer's residual database state.
- Match the approved target collation, compatibility level where supported, snapshot/isolation settings, and time-zone assumptions as closely as the local engine permits.

## Spring profiles

Keep common behavior in the default configuration and isolate environment-specific connection details:

```text
application.yml
application-local.yml
application-test.yml
application-azure.yml
```

Rules:

- Use environment variables or an approved local secret mechanism for local connection values.
- Commit safe property names and examples, not passwords or working connection strings containing secrets.
- Keep `local` and `test` profiles disabled unless explicitly selected.
- Use Testcontainers-provided connection details for automated tests rather than fixed ports.
- Keep Azure identity, server name, database name, and certificate behavior in the Azure deployment configuration.
- Fail startup when required configuration is missing. Do not silently fall back from Azure to a local database.

## Testcontainers

- Use the Testcontainers Microsoft SQL Server module and an approved, pinned Microsoft image tag or digest.
- Accept the Microsoft SQL Server container license deliberately using the approved Testcontainers mechanism; do not conceal acceptance in an unrelated script.
- Let the runtime allocate host ports to avoid collisions.
- Wait for database readiness before migrations and tests.
- Run Flyway/Liquibase automatically against the new container before repository or application tests.
- Reuse containers only when it cannot leak state between tests; prefer a known empty state for parity-sensitive cases.
- Keep the Microsoft JDBC driver as a separate application/test dependency.
- Detect unavailable Docker/container runtime and report the test as unavailable or failed according to CI policy; never silently replace it with H2.

## Manual local database

If Docker Compose or an installed instance is used:

- Store local orchestration under `target/react-spring-azure-sql/database/local/`.
- Pin container images. Do not use an unreviewed floating image in CI.
- Bind only required local ports and do not expose the instance to untrusted networks.
- Use a dedicated nonproduction database and least-privilege application/migration users.
- Put passwords in environment variables or an ignored local secrets file.
- Provide health checks and deterministic start, migrate, seed, test, reset, and stop commands.
- Seed sanitized synthetic characterization data only.
- Make destructive reset commands refuse nonlocal hosts and databases.

## TLS

- Keep JDBC encryption enabled.
- Prefer a locally trusted development certificate.
- If a self-signed local instance requires `trustServerCertificate=true`, restrict that setting to the `local` or disposable `test` profile and document the exception.
- Require certificate validation in Azure and all shared environments. Never copy the local trust bypass into production configuration.

## Required local checks

Run:

- Full clean migration and repeat migration behavior.
- Repository queries and parameter mappings.
- Exact decimal, date/time, null/blank, fixed-character, collation, and ordering cases.
- Keys, checks, defaults, identity/sequence, and optimistic-concurrency behavior.
- Transaction, rollback, savepoint, isolation, lock, deadlock, duplicate, and idempotency cases.
- Representative full-stack oracle cases through React and Spring Boot.

Record the database engine/image, configuration, migration version, commands, results, and known differences from Azure SQL Database.

## Azure compatibility gate

A local SQL Server accepts features that Azure SQL Database may reject. Before approving a slice:

- Build or assess the schema for the Azure SQL Database target platform.
- Scan for unsupported instance-level, operating-system, filesystem, cross-database, SQL Agent, CLR, linked-server, and Managed Instance features.
- Deploy the same migration chain to an empty Azure SQL Database.
- Rerun schema, integration, concurrency, security, performance, and parity tests required by the release gate.

Local passing tests are inner-loop evidence, not the final database acceptance result.
