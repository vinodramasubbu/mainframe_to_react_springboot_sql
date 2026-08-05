# ADR 0001: TASK-SURV-001 target stack and security boundary

- Status: Provisional, implemented for slice validation; architecture and security owner approval required before deployment
- Date: 2026-08-05
- Scope: TASK-SURV-001 survivor entitlement inquiry

## Context

SURVINQ is a read-only CICS pseudo-conversational inquiry. It accepts a 12-character claim ID and 10-character beneficiary ID, joins claim, entitlement, and beneficiary data, then applies a fixed priority to status labels and messages. No approved enterprise identity provider, target runtime version ADR, data profile, or runtime oracle was supplied.

## Decision

- Use React 19 with TypeScript and Vite for a single accessible inquiry route.
- Use Java 17 bytecode, Spring Boot 3.5.4, Spring MVC, direct JDBC through `JdbcClient`, Flyway, and the Microsoft SQL Server JDBC driver.
- Use a Spring Boot modular-monolith package structure separating domain, application, API, and infrastructure adapters.
- Require an authenticated JWT bearer token and authority `SCOPE_survivor.inquiry` for the API. Issuer and audience configuration are deployment inputs; no local authentication bypass is provided.
- Keep money as scale-two decimal strings in JSON and `BigDecimal` in Java.
- Use RFC 9457 problem responses with stable codes and a correlation ID. Unexpected errors log the cause and correlation ID.
- Use one Flyway migration chain for local SQL Server and Azure SQL Database. Disable automatic ORM schema generation by not using ORM.
- Use Testcontainers SQL Server for repository integration tests when an approved Docker engine is available.

## Consequences

- The frontend and backend can be tested independently with mocked authenticated requests, but live sign-in is blocked until identity configuration and ownership are approved.
- Direct JDBC keeps the legacy join and fixed identifier behavior visible and testable.
- Exact parity remains unproven until sanitized legacy inquiry outcomes and an Azure SQL validation environment are supplied.

## Required follow-up approvals

- Security owner: identity provider, issuer, audience, scope/role mapping, token handling, audit, and CORS origins.
- Platform owner: dependency baselines, hosting, Azure SQL compatibility level, and managed-identity JDBC configuration.
- Data owner: collation, Unicode conversion, timestamp time-zone semantics, fixed-character padding, and source data profile.