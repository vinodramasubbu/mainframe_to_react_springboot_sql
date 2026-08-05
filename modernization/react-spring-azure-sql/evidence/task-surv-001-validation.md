# TASK-SURV-001 validation record

- Capability: Survivor entitlement inquiry
- Legacy entry point: CICS SI01 / SURVINQ
- Target components: React inquiry page, GET inquiry API, Spring application/domain service, JDBC repository, SURVDEMO Azure SQL schema
- Validation date: 2026-08-05
- Approval status: Component validation only; not approved for deployment or parity sign-off

## Traceability covered

| Legacy behavior | Target mapping | Executed evidence |
|---|---|---|
| Claim ID and beneficiary ID inquiry | React `SurvivorInquiryPage`; OpenAPI `inquireSurvivorEntitlement`; `SurvivorInquiryService` | React component submission test; Spring controller contract test |
| Relationship and entitlement presentation | `EntitlementPresentation` | Five domain characterization tests |
| Read-only claim/beneficiary/entitlement join | `JdbcSurvivorEntitlementRepository` | Integration test implemented; execution unavailable because Docker was not running |
| Exact monthly amount | OpenAPI decimal string; Java `BigDecimal`; React string display | Domain, controller, and component assertions for `1250.00` |
| Enter submits and initial claim focus | Native form submission and initial focus | Component focus tests; desktop/mobile browser inspection |
| PF3 exit | Explicit Clear action | Component clear-and-focus test; business/UX approval still required |
| Protected result fields | Read-only result definition list | Component and browser inspection |
| Authentication and survivor inquiry authority | JWT resource server and `SCOPE_survivor.inquiry` | Spring 401/403 tests; browser fail-closed identity-gate test |
| Safe errors and correlation | RFC 9457-style problem contract and correlation filter | Spring not-found, unavailable, authentication, and authorization tests |

## Commands and actual results

| Command/check | Result |
|---|---|
| Maven backend `test` | Passed: 10 tests passed; 1 SQL Server integration test skipped because Docker was unavailable |
| Frontend `npm test` | Passed: 9 component, focus, state, and axe accessibility tests |
| Frontend `npm run build` | Passed: OpenAPI types generated, strict TypeScript compiled, Vite production bundle built |
| Frontend `npm run lint` | Passed with no reported findings |
| Frontend `npm audit --omit=dev` | Passed: 0 production vulnerabilities |
| Full frontend `npm audit` | Blocked: 14 development-only advisories in current Vite/esbuild, ESLint/typescript-eslint, and OpenAPI generation dependency paths; nonbreaking audit repair was unavailable |
| Compose `config --quiet` | Passed using the committed safe example environment |
| PowerShell parser check for `initialize.ps1` | Passed |
| Playwright identity-gate test | Passed: 1 Chromium test; proves only fail-closed behavior before identity integration |
| Browser inspection at 1440x900 and 390x844 | Passed for horizontal overflow, panel overlap, responsive stacking, initial focus, and focused error alert |

## Unverified and blocked gates

- No approved sanitized legacy input/output oracle exists. Legacy equivalence is not established.
- Docker Desktop was unavailable. The Flyway chain and JDBC query were not executed against local SQL Server in this validation run.
- No Azure SQL Database environment was supplied. Compatibility, metadata, migration, query-plan, concurrency, security, recovery, and performance gates were not run.
- Identity issuer, audience, role/scope mapping, browser PKCE or BFF design, and audit policy remain unapproved. The frontend intentionally has no token-storage or authentication bypass.
- Authenticated browser-to-Spring-to-database success, forbidden, not-found, unavailable, timeout, and correlation E2E cases are pending identity and seeded database environments.
- Automated axe checks passed, but manual keyboard, zoom/reflow, screen-reader, contrast, and supported-browser approval remains outstanding.
- Development dependency advisories require patched stable upstream releases or an approved, separately validated major toolchain upgrade.

## Rollback

The slice is additive and read-only. Until routing and identity are approved, SI01 remains authoritative. Deployment rollback is to remove target routing and retain target logs and read-only data only under the approved retention policy.

## Required approvals

- Business/mainframe owner: approved characterization cases and message/keyboard/Clear behavior.
- Security owner: identity flow, issuer/audience, authority mapping, token handling, CORS, audit, and protected-data policy.
- Data/platform owner: Azure SQL collation, Unicode and fixed-character behavior, compatibility level, managed identity, hosting, and migration execution.
- Accessibility/UX owner: responsive workflow, keyboard path, error announcements, and assistive-technology evidence.