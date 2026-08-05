---
applyTo: "target/react-spring-azure-sql/database/local/**,target/react-spring-azure-sql/backend/src/test/**,target/react-spring-azure-sql/backend/src/test/resources/**"
---

# Local SQL Server testing rules

- Use local SQL Server only for development and automated testing; Azure SQL Database remains the deployment and final compatibility target.
- Prefer the Testcontainers Microsoft SQL Server module for Spring repository and integration tests. Use an approved Docker Compose or installed Developer/Express instance for manual full-stack development when required.
- Use the same Microsoft JDBC driver, persistence mappings, T-SQL, and Flyway/Liquibase migration chain locally and in Azure.
- Do not add local-only schema objects, migrations, SQL branches, relaxed constraints, or alternate database dialects.
- Do not use H2, HSQLDB, Derby, SQLite, or mocked repositories as database-parity evidence.
- Start automated tests from a clean disposable database/container and run the complete migration chain before test data is loaded.
- Pin the approved SQL Server container image tag or digest and record deliberate acceptance of the Microsoft container license.
- Use runtime-assigned ports for automated tests and deterministic health/readiness checks.
- Keep local connection values in environment variables or ignored local secrets. Commit only safe examples.
- Keep JDBC encryption enabled. Restrict `trustServerCertificate=true`, if required for a self-signed local test certificate, to the local/test profile; prohibit it in Azure and shared environments.
- Never silently fall back from Azure configuration to a local database.
- Put manual orchestration and safe start/migrate/seed/test/reset/stop support under `target/react-spring-azure-sql/database/local/`.
- Make reset operations verify that the target is explicitly local before deleting data.
- Use sanitized synthetic fixtures only; never copy production records into the local database.
- Run Azure SQL schema compatibility checks and the required migration/integration/parity suite against Azure SQL before accepting the slice.

## Efficient local run (reduce rework)

- Cache the Maven repository across builds. When building in a throwaway Maven container, mount the host `.m2` (`-v "$HOME/.m2:/root/.m2"`) so dependencies are not re-downloaded on every build.
- Keep the schema qualifier identical across the migration DDL, the seed SQL, and every persistence query. Schema drift (for example `appschema.*` versus `dbo.*`) does not raise a clear error; it surfaces only as a generic 5xx from the API error handler and wastes a debugging cycle.
- Stop the running application process before repackaging its jar; an open jar makes the Spring Boot repackage step fail while renaming the artifact.
- Treat the app-owned migration chain as the single schema source locally; do not disable migrations as a shortcut, and make the local seed target the migrated schema rather than a hand-built copy.
- Verify readiness with a real API call that also touches the database (for example the primary inquiry/search endpoint), not just a port check, so schema/data faults are caught immediately.
- When launching the backend detached for log capture, redirect standard output and standard error to separate files.
