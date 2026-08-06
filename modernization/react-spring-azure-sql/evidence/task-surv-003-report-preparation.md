# TASK-SURV-003 payment report preparation

## Work packet

| Field | Value |
|---|---|
| Capability/slice | Prepare payment report detail records after an eligible monthly calculation outcome, without physical publication |
| Actors and entry points | Monthly batch successor; `MonthlyBatchProcess.run` and pure `PaymentReportPreparer.prepare` invocation |
| Legacy artifacts and revision | Extracted `DEV1/SURVDEMO/CNTL/PAYSORT.ctl` and `JCL/SURVMON.jcl`; extraction revision is unavailable |
| Screen/task IDs | TASK-SURV-003 |
| Business-rule IDs | RULE-SURV-017 |
| Interfaces and data | Calculation return code and run ID; ordered Java strings representing complete 120-character payment records; immutable ordered detail-record result |
| Oracle/test IDs | `MonthlyBatchProcessTest`, `StagedMonthlyBatchOutputAdapterTest`, and `PaymentReportPreparerTest`; source-derived characterization only, with no approved legacy runtime oracle |
| Intentional behavior changes | Malformed-length records fail immediately instead of reaching a sort utility |
| React components/routes | None |
| OpenAPI operations/errors | None |
| Spring use cases/domain rules | None; framework-independent infrastructure transformation |
| Azure SQL objects/migrations | None |
| External integrations | Scheduler-invocable command path retained; physical report publication deferred |
| Rollback | Remove the coordinator successor wiring, staged report snapshot, preparer, focused tests, and this rule mapping; TASK-SURV-002 calculation staging remains independent |
| Approvals | User authorized implementation by proceeding with TASK-SURV-003 |

## Source mapping

`PAYSORT.ctl` supplies the complete implemented transformation:

- `INCLUDE COND=(1,1,CH,EQ,C'D')` maps to retaining only records whose first character is `D`.
- `SORT FIELDS=(18,12,CH,A,30,10,CH,A)` maps to ascending character comparison of zero-based substrings `[17,29)` and `[29,39)`.
- `OPTION EQUALS` maps to stable encounter order when both keys are equal.

`SURVMON.jcl` supplies the successor condition and physical-record boundary. `MonthlyBatchProcess` now invokes report preparation for calculation return codes 0 and 4 and suppresses it for technical return code 12, preserving the source condition that PAYRPT runs only when the preceding return code is below 8. `StagedMonthlyBatchOutputAdapter` applies the preparer to the completed payment stream and retains an immutable in-memory detail report.

The source declares fixed-block LRECL 120, but physical bytes, encoding, destination, retention, atomic disposition, and delivery cannot be inferred from 120 Java characters. Those behaviors are not implemented and remain blocked by RISK-SURV-002 and RISK-SURV-004.

## Acceptance and validation

The focused tests cover mixed header/detail/trailer input, detail-only output, ascending claim and beneficiary keys, stable order for equal keys, immutable output, exact 120-character records, rejection of malformed record length, staged snapshot integration, invocation for return codes 0 and 4, and suppression for return code 12.

Validation executed on 2026-08-05 from `target/react-spring-azure-sql/backend` using Maven 3.9.9 and Java 17:

- `mvn -Dtest=MonthlyBatchProcessTest test`: 5 tests passed, 0 failures, 0 errors, 0 skipped.
- `mvn -Dtest=MonthlyBatchProcessTest,StagedMonthlyBatchOutputAdapterTest,PaymentReportPreparerTest test`: 9 tests passed, 0 failures, 0 errors, 0 skipped.
- `mvn test`: 39 tests discovered, 37 executed and passed, 0 failures, 0 errors, and 2 Docker-dependent database integration tests skipped because Testcontainers could not establish a Docker connection.

Current parity status: the source-derived transformation and return-code gate are implemented. Runtime equivalence is not claimed because no approved legacy output oracle, source encoding metadata, or downstream publication contract is available.