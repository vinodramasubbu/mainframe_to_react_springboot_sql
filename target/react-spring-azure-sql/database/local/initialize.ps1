$ErrorActionPreference = "Stop"

if ($env:SURVDEMO_DB_HOST -and $env:SURVDEMO_DB_HOST -notin @("localhost", "127.0.0.1")) {
    throw "Refusing to initialize a nonlocal SQL Server host."
}

Push-Location $PSScriptRoot
try {
    docker compose up -d --wait
    $createDatabase = '/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -b -Q "IF DB_ID(N''SURVDEMO'') IS NULL CREATE DATABASE [SURVDEMO];"'
    docker compose exec -T sqlserver bash -c $createDatabase
    if ($LASTEXITCODE -ne 0) {
        throw "Local SURVDEMO database initialization failed."
    }
}
finally {
    Pop-Location
}

Write-Host "Local SURVDEMO database is ready. Run the backend with the local profile so Flyway applies the authoritative migration chain."