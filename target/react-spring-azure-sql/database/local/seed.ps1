$ErrorActionPreference = "Stop"

if ($env:SURVDEMO_DB_HOST -and $env:SURVDEMO_DB_HOST -notin @("localhost", "127.0.0.1")) {
    throw "Refusing to seed a nonlocal SQL Server host."
}

Push-Location $PSScriptRoot
try {
    $seedSql = Get-Content -Raw (Join-Path $PSScriptRoot "seed-inquiry.sql")
    $sqlcmd = '/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -d SURVDEMO -b'
    $seedSql | docker compose exec -T sqlserver bash -c $sqlcmd
    if ($LASTEXITCODE -ne 0) {
        throw "Local SURVDEMO sample-data seed failed."
    }
}
finally {
    Pop-Location
}

Write-Host "Synthetic TASK-SURV-001 inquiry data loaded."