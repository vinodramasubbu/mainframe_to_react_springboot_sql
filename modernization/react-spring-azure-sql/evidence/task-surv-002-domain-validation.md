# TASK-SURV-002 domain and batch validation record

- Capability: Monthly survivor benefit calculation, application orchestration, JDBC persistence, scheduler-invocable process boundary, authenticated on-demand operator invocation, and staged fixed output records
- Legacy entry points: SURVVALID and SURVCALC
- Target components: Dependency-free Java rules, batch application service and ports, Spring transaction adapters, Azure SQL-compatible JDBC adapters, a host-neutral command-line process adapter, an authenticated Spring MVC adapter, a React operator workflow, a pure fixed-record formatter, and an in-memory staging adapter
- Validation date: 2026-08-05
- Approval status: Source-derived component validation only; not approved for deployment or parity sign-off

## Traceability covered

| Rule IDs | Legacy behavior | Target mapping | Executed evidence |
|---|---|---|---|
| RULE-SURV-001 through RULE-SURV-006 | Validate claim status, beneficiary status, relationship, benefit percentage, offset, and base amount in first-failure order | `SurvivorValidationService`, `ValidationInput`, and `ValidationResult` | Valid path, every reason code, percentage boundaries, every accepted relationship, and a multiple-failure priority case |
| RULE-SURV-007 | Round family cap from base monthly benefit and family maximum percentage | `MonthlyBenefitCalculationEngine.percentageOf` | Exact cap-boundary and cap-crossing cases |
| RULE-SURV-008 | Round gross benefit before applying offset | `MonthlyBenefitCalculationEngine.calculate` | `.005` source-derived rounding boundary produces `0.51` before a `0.01` offset |
| RULE-SURV-009 | Subtract offset after gross calculation; nonpositive net produces Z1 | `ExceptionDecision` with gross and net values | Exact zero and negative-net cases |
| RULE-SURV-010 | Existing payment for claim, beneficiary, and month produces D1 | `MonthlyCalculationInput.duplicatePayment` input fact | Validation-before-duplicate priority and zero expected/actual amounts |
| RULE-SURV-011 | Allocate by claim then beneficiary ID; reduce a cap-crossing payment with status C; later records produce F1 | Ordered calculation and immutable `PaymentDecision`/`ExceptionDecision` records | Unordered three-beneficiary input produces R, C, then F1 in beneficiary-ID order |
| RULE-SURV-012 | Update an active entitlement only when its numeric version matches and require exactly one affected row | `MonthlyBenefitBatchService`, `MonthlyBatchEntitlementUpdate`, and `JdbcMonthlyBatchDataAdapter` | Fake-port conflict returns zero rows, aborts remaining work, and produces technical severity 12 |
| RULE-SURV-015 | Return severity 0 for a clean run, 4 for business exceptions, and 12 for technical failure | `MonthlyBatchOutcome` | Clean, business-exception, and optimistic-conflict orchestration cases |
| RULE-SURV-016 | Commit run status R separately; commit calculation work and status C atomically; after rollback mark the run F separately | `JdbcMonthlyBatchRunLifecycle`, `SpringMonthlyBatchTransaction`, and `MonthlyBenefitBatchService` | Ordered fake transaction/lifecycle events; SQL Server integration test is implemented but execution is Docker-blocked |
| IFACE-SURV-001 | Interpret one fixed 20-character control record as an 8-character `YYYYMMDD` calculation date followed by a 12-character run ID | `MonthlyBatchProcess` and the explicit `--survdemo.batch.control-record=` launch mode | Valid legacy example, invalid date, invalid length, invalid date characters, normal web-mode isolation, and exact 0/4/12 propagation |
| IFACE-SURV-002 | Format and stage payment H/D/T records and pipe-delimited exception records as exactly 120 characters using `SURVOUT` and `SURVCALC` field definitions; preserve the source write positions | `MonthlyBatchOutputPort`, `MonthlyBatchRecordFormatter`, `StagedMonthlyBatchOutputAdapter`, and `MonthlyBenefitBatchService` | Exact golden strings and lengths; separate immutable staged streams; payment output after insert/update; exception output before insert; trailer before run completion; discard after transaction rollback |
| IFACE-SURV-003 | Intentionally add an operator-triggered entry point without duplicating calculation rules or changing RC 0/4/12 outcomes | OpenAPI `POST /api/v1/monthly-benefit-runs`, `MonthlyBenefitRunController`, `OnDemandMonthlyBatchService`, generated TypeScript types, and `MonthlyBenefitRunPage` | Scope enforcement, request validation, exact-string money mapping, structured payment/exception response, client-side validation/focus, and rendered outcome/detail rows |

## Commands and actual results

| Command/check | Result |
|---|---|
| Maven `-Dtest=SurvivorValidationServiceTest test` | Passed: 3 tests, 0 failures, 0 errors |
| Maven `-Dtest=MonthlyBenefitCalculationEngineTest,SurvivorValidationServiceTest test` | Passed: 8 tests, 0 failures, 0 errors |
| Maven `-Dtest=MonthlyBenefitBatchServiceTest test` | Passed: 3 tests, 0 failures, 0 errors |
| Maven `-Dtest=MonthlyBenefitBatchServiceTest,JdbcMonthlyBenefitBatchIntegrationTest test` | Passed: 3 executable tests; 1 SQL Server integration test skipped because Docker was unavailable |
| Maven backend `test` | Passed: 32 tests, 0 failures, 0 errors; 2 SQL Server integration tests skipped because Docker was unavailable |
| Maven `-Dtest=MonthlyBatchProcessTest,SurvdemoApplicationTest test` | Passed: 4 tests, 0 failures, 0 errors |
| Maven `-Dtest=MonthlyBatchRecordFormatterTest test` | Passed: 3 tests, 0 failures, 0 errors |
| Maven `-Dtest=MonthlyBenefitBatchServiceTest,StagedMonthlyBatchOutputAdapterTest,MonthlyBatchRecordFormatterTest test` | Passed: 8 tests, 0 failures, 0 errors |
| Maven `-Dtest=MonthlyBenefitRunControllerTest test` | Passed: 3 tests, 0 failures, 0 errors |
| Maven backend `test` after on-demand integration | Passed: 37 tests discovered, 35 executed successfully, 2 SQL Server integration tests skipped because Docker was unavailable |
| Frontend focused `npm test -- --run --pool=threads src/features/monthly-benefit-run/MonthlyBenefitRunPage.test.tsx` | Passed: 2 tests, 0 failures |
| Frontend `npm test -- --run --pool=threads` | Passed: 13 tests, 0 failures |
| Frontend `npm run lint` | Passed with no reported errors |
| Frontend `npm run build` | Passed: OpenAPI generation, TypeScript build, and Vite production build |

Maven was not available on the shell `PATH`; validation used the existing Maven 3.9.9 installation under the user Maven wrapper cache.

## Implemented batch boundary

- Eligibility selection uses the legacy active/date predicates, deterministic claim/beneficiary ordering, repeatable-read calculation transaction, and an update lock on entitlement rows.
- Duplicate detection queries the benefit-period key before calculation; the existing unique index remains the final database enforcement.
- Payment and exception inserts, four-digit payment identifiers, optimistic entitlement updates, completed run totals, and return outcomes are connected through application ports.
- Run creation/failure use independent `REQUIRES_NEW` transactions. Calculation persistence and completion status use one transaction.
- The existing V1 migration already contains the complete legacy table and index set, so this packet makes no schema change.
- The React operator surface invokes the same authoritative server-side batch use case through `POST /api/v1/monthly-benefit-runs`; it displays returned exact decimal strings and does not reproduce calculation, validation, authorization, or state-transition rules.
- The HTTP adapter requires `SCOPE_survivor.batch.run`, accepts an ISO calculation date and exactly 12 non-space run-ID characters, and returns synchronous RC 0, 4, or 12 outcomes with staged structured payment and exception rows. RC 12 remains a process outcome in a successful HTTP response rather than being converted into a transport error.
- The page warns operators to use a new run ID for every attempt. Server-side uniqueness and existing failure-state behavior remain authoritative.
- An external scheduler can invoke the packaged application with `--survdemo.batch.control-record=YYYYMMDDRRRRRRRRRRRR`. That explicit argument selects non-web mode, invokes the batch once, and terminates with return code 0, 4, or 12.
- The formatter reproduces the source-declared 120-character payment header, detail, and trailer layouts and the 120-character exception layout without choosing a filesystem, output path, or publication protocol.
- Payment amounts use zero-filled implied-decimal fields. Exception amounts use the source `-ZZZ,ZZZ,ZZ9.99` edited representation. Beneficiary names preserve the COBOL `(1:30)` truncation behavior.
- Eligibility selection now retains the joined beneficiary name for payment detail formatting; relationship continues to come from the calculation input.
- The application opens staged output inside the calculation transaction. It stages a payment detail only after payment insert and successful optimistic entitlement update, stages an exception before its database insert, and stages the trailer before run completion, matching `SURVCALC` call order.
- A technical failure rolls database work back and discards all staged records before the independent failed-run update. Successful records remain available only as immutable in-memory snapshots; no durable publication is claimed.

## Scope intentionally deferred

- Filesystem writing, character encoding, line framing, sorted report, atomic/partial-file disposition, and downstream delivery remain deferred until the output encoding and consumer contract are approved.
- Overlap prevention by benefit month, restart/rerun policy, deadlock behavior, production scheduler/host selection, managed identity, calendar configuration, metrics, and alerts belong to the remaining WP-SURV-05/WP-SURV-06 work.
- The on-demand route does not approve concurrent execution, replace the production scheduler, or resolve coexistence. Production enablement remains blocked until overlap prevention, authorization ownership, and operational controls are approved.

## Unverified and blocked gates

- No approved sanitized legacy input/output/post-state oracle exists. Legacy equivalence is not established.
- Docker was unavailable, so neither SQL Server Testcontainers integration test executed. The new batch integration test compiles and covers the intended migrated schema post-state, but no database behavior is claimed from a skipped test.
- Azure SQL Database migration, locking, query-plan, rollback, concurrency, and performance validation have not run.
- COBOL compiler options and runtime rounding configuration are unavailable. `RoundingMode.HALF_UP` at scale 2 is a source-derived provisional interpretation of positive `COMPUTE ... ROUNDED` values and requires mainframe characterization approval.
- No evidence case establishes whether inconsistent base amount or family maximum percentage values can occur within one claim. The engine follows the sorted legacy flow and establishes the family cap from the first entitlement in each claim.
- The source fields constrain monetary and percentage precision, but trust-boundary range/scale enforcement is deferred until the batch input and persistence contracts are approved.
- The source supports a four-digit payment sequence. Behavior beyond 9,999 successful payments cannot be finalized in a pure engine that does not assign payment IDs.
- Golden tests establish 120 Java characters for the source's current ASCII-compatible values. They do not establish 120 encoded bytes, EBCDIC/Unicode equivalence, record delimiters, or downstream transfer compatibility.
- Staged records are process-memory artifacts only. A process exit loses them, so they are not a downstream delivery mechanism or parity evidence.

## Rollback

The packet is additive and introduces no migration. The on-demand portion can be rolled back by removing the API route, result-reading port, React monthly-run navigation/view, and their tests while leaving the scheduler-invocable batch boundary intact. No production scheduler is configured to invoke the target process, and legacy SURVMON remains authoritative.

## Required approvals

- Business/mainframe owner: validation priority, exact reason codes, decimal rounding, family-cap allocation, and approved oracle cases.
- Data/platform owner: duplicate-detection contract, ordering guarantees, precision constraints, and Azure SQL implementation.
- Security/operations owners: assignment of `survivor.batch.run`, operator eligibility, audit expectations, concurrency controls, and on-demand production enablement.
- Operations owner: run totals, files, return outcomes, restart behavior, and scheduler integration in later work packets.
