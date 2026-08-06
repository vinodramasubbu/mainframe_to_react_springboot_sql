# SURVDEMO target application

This folder contains the modernized SURVDEMO application:

- `frontend/`: React and TypeScript operator UI
- `backend/`: Spring Boot API, domain logic, and SQL adapters
- `database/local/`: local SQL Server orchestration and synthetic seed data
- `tests/e2e/`: Playwright browser checks

Azure SQL Database is the production database target. The local SQL Server container is for development and testing only.

## Prerequisites

Install and verify:

- JDK 17 or later
- Maven 3.9 or later
- Node.js 22.12 or later and npm
- Docker Desktop with Linux containers enabled
- PowerShell 7
- `sqlcmd` for direct database inspection (optional)

Run these checks from the repository root:

```powershell
java -version
mvn -version
node --version
npm --version
docker version
```

If `mvn` is not on `PATH`, use the Maven executable installed on your machine in place of `mvn` in the commands below.

## Quick start: sample UI

Sample mode is the fastest way to exercise the React workflows. It uses fixed development fixtures and does not call Spring Boot or SQL Server.

```powershell
Set-Location target/react-spring-azure-sql/frontend
npm install
npm run dev -- --host 127.0.0.1 --port 5173
```

Open:

```text
http://127.0.0.1:5173/?sampleData=true
```

Use the **Monthly run** page to submit a 12-character run ID and a calculation date. Sample mode is enabled only by the Vite development build and is not database-parity evidence.

## Start local SQL Server

From the repository root:

```powershell
Set-Location target/react-spring-azure-sql/database/local
Copy-Item .env.example .env
```

Edit `.env` and replace the placeholder with a strong local-only password. Do not commit `.env`.

Start SQL Server and create the empty `SURVDEMO` database:

```powershell
./initialize.ps1
```

The container uses SQL Server 2022 CU18 on `127.0.0.1:1433` by default. Flyway in the backend owns schema creation.

## Start the backend

Set the backend environment in the same PowerShell session. Replace the password and issuer URI with approved local values:

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"
$env:SURVDEMO_DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SURVDEMO;encrypt=true;trustServerCertificate=true"
$env:SURVDEMO_DB_USERNAME = "sa"
$env:SURVDEMO_DB_PASSWORD = "<password-from-database-local-.env>"
$env:SURVDEMO_JWT_ISSUER_URI = "<approved-development-issuer>"
$env:SURVDEMO_ALLOWED_ORIGIN = "http://127.0.0.1:5173"

Set-Location ../../../backend
mvn spring-boot:run
```

The default backend URL is `http://127.0.0.1:8080`. If port 8080 is occupied, start it on another port:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Check readiness:

```powershell
Invoke-RestMethod http://127.0.0.1:8080/actuator/health
```

On first startup, Flyway applies the complete migration chain from `backend/src/main/resources/db/migration/`. Later startups validate the migration history before accepting requests.

## Load synthetic inquiry data

Start the backend once so Flyway creates the schema. In another PowerShell session, set the same local database password and run:

```powershell
$env:SURVDEMO_DB_PASSWORD = "<password-from-database-local-.env>"
Set-Location target/react-spring-azure-sql/database/local
./seed.ps1
```

This seed is synthetic and supports the survivor inquiry examples documented in `database/local/README.md`.

## Call the real monthly-run API

The endpoint requires a valid bearer token containing the scope `survivor.batch.run`. Use a new 12-character run ID for each attempt.

```powershell
$token = "<approved-development-access-token>"
$headers = @{
    Authorization = "Bearer $token"
    "X-Correlation-ID" = "SURV-LOCAL-TEST-001"
}
$body = @{
    runId = "RUN202608002"
    calculationDate = "2026-07-31"
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri "http://127.0.0.1:8080/api/v1/monthly-benefit-runs" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $body
```

Expected return-code meanings:

| Return code | Meaning |
|---:|---|
| `0` | Completed without business exceptions |
| `4` | Completed with one or more business exceptions |
| `12` | Technical failure; calculation work was rolled back and the run was marked failed |

For completed runs, reconcile the response with these tables:

- `SURVDEMO.CALC_RUN`
- `SURVDEMO.BENEFIT_PAYMENT`
- `SURVDEMO.CALC_EXCEPTION`
- `SURVDEMO.SURVIVOR_ENTITLEMENT`

Do not retry a failed request with the same run ID.

## Run the scheduler command path

The non-web command path accepts the legacy 20-character control record: an 8-character calculation date in `YYYYMMDD` format followed by a 12-character run ID. Use the same local database environment described above, then run:

```powershell
Set-Location target/react-spring-azure-sql/backend
mvn spring-boot:run "-Dspring-boot.run.arguments=--survdemo.batch.control-record=20260831SRV202608001"
```

The process preserves the calculation return code. Return codes 0 and 4 invoke TASK-SURV-003 payment report preparation; return code 12 suppresses it. The prepared report contains only 120-character detail records sorted by claim ID and beneficiary ID, with encounter order preserved for equal keys.

The report is currently an immutable in-memory snapshot. No physical file is published because the source extraction does not establish the required encoding, destination, retention, atomic disposition, or downstream delivery contract. Running this command is therefore calculation and report-preparation evidence, not file-delivery or legacy-parity evidence.

## Run automated tests

### Backend

```powershell
Set-Location target/react-spring-azure-sql/backend
mvn test
```

Run only the monthly batch database integration test:

```powershell
mvn -Dtest=JdbcMonthlyBenefitBatchIntegrationTest test
```

Run the focused TASK-SURV-003 report-preparation checks:

```powershell
mvn "-Dtest=MonthlyBatchProcessTest,StagedMonthlyBatchOutputAdapterTest,PaymentReportPreparerTest" test
```

The database integration tests use Testcontainers and apply Flyway to a disposable SQL Server. They are skipped when Testcontainers cannot detect Docker. A skipped test is not a passing database test.

### Frontend

```powershell
Set-Location target/react-spring-azure-sql/frontend
npm install
npm run test -- --pool=threads
npm run lint
npm run build
```

`npm run build` regenerates the TypeScript API types from the approved OpenAPI contract before compiling the application.

### Browser identity-gate test

Start the frontend on port 5173, then run:

```powershell
Set-Location target/react-spring-azure-sql/tests/e2e
npm install
npx playwright install chromium
npm test
```

The current Playwright test proves that the UI fails closed when no identity provider is configured. It does not prove an authenticated business workflow or database parity.

## Real browser mode status

The normal frontend URL, without `?sampleData=true`, intentionally requires an access-token provider. That provider is not configured yet, so the UI reports `SURV-IDENTITY-NOT-CONFIGURED` instead of calling the API.

The Vite development proxy currently targets `https://localhost:8443`, while the basic local backend instructions above use HTTP port 8080. Before claiming a real browser-to-database test, configure the approved identity integration and align the Vite proxy with the secured backend endpoint. Do not put client secrets or reusable access tokens in frontend source, environment files exposed by Vite, browser storage, or this repository.

## Stop local services

Stop Spring Boot and Vite with `Ctrl+C` in their terminals. Stop SQL Server without deleting its volume:

```powershell
Set-Location target/react-spring-azure-sql/database/local
$env:SURVDEMO_DB_PASSWORD = "<password-from-database-local-.env>"
docker compose down
```

Delete the local volume only for a deliberate clean reset:

```powershell
docker compose down --volumes
```

This permanently removes the local database contents.

## Validation boundary

Passing local tests demonstrates the React, Spring Boot, migration, and SQL Server development paths. It does not establish Azure SQL Database compatibility or production readiness. Apply the same migration chain and required security, schema, query-plan, concurrency, rollback, and parity checks to an approved empty Azure SQL Database before release acceptance.
