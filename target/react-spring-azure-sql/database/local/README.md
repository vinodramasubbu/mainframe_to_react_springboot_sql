# Local SQL Server

This harness is for local development only. It uses the same SQL Server 2022 CU18 image as the repository integration test and deliberately accepts the Microsoft SQL Server container license through `ACCEPT_EULA=Y`.

1. Copy `.env.example` to `.env` and set a strong local-only password.
2. Run `./initialize.ps1` from PowerShell.
3. Set the backend environment:

   ```powershell
   $env:SPRING_PROFILES_ACTIVE = "local"
   $env:SURVDEMO_DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SURVDEMO;encrypt=true;trustServerCertificate=true"
   $env:SURVDEMO_DB_USERNAME = "sa"
   $env:SURVDEMO_DB_PASSWORD = "<the local .env password>"
   $env:SURVDEMO_JWT_ISSUER_URI = "<approved development issuer>"
   ```

4. Start the backend once so Flyway applies the authoritative migration chain.
5. Run `./seed.ps1` to load the synthetic inquiry cases below.

Flyway in the backend owns schema creation. Do not add a second schema or seed path here. Stop the server with `docker compose down`; remove the local volume only when a deliberate clean reset is required.

## Inquiry samples

All records are synthetic and may be reloaded safely; the seed replaces only these known IDs.

| Claim ID | Beneficiary ID | Expected status | Expected message | Monthly amount |
|---|---|---|---|---:|
| `CLM000000001` | `BENE000001` | ACTIVE | ENTITLEMENT FOUND | 1250.00 |
| `CLM000000002` | `BENE000002` | SUSPENDED | ENTITLEMENT IS SUSPENDED | 1180.00 |
| `CLM000000003` | `BENE000003` | ENDED | ENTITLEMENT HAS ENDED | 450.00 |
| `CLM000000004` | `BENE000004` | CANCELLED | ENTITLEMENT IS CANCELLED | 1000.00 |
| `CLM000000005` | `BENE000005` | NOT_APPROVED | CLAIM IS NOT APPROVED | 1375.00 |
| `CLM000000006` | `BENE000006` | INELIGIBLE | BENEFICIARY IS NOT ELIGIBLE | 735.00 |

Use claim `CLM999999999` and beneficiary `BENE999999` for the not-found path.

For frontend-only testing without an identity provider or backend connection, open
`http://127.0.0.1:5173/?sampleData=true`. This development-only mode returns fixed
contract fixtures matching the rows above. The normal URL continues to require an
authenticated API token, and production builds cannot enable sample mode.

Local SQL Server results are inner-loop evidence only. Azure SQL Database migration, security, query-plan, concurrency, and parity gates remain required.