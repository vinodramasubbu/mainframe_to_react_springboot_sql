---
name: validate-mainframe-modernization
description: "Independently validate a React, Spring Boot, and Azure SQL modernization slice against approved COBOL and mainframe evidence. Use for traceability review, differential parity, test criticism, accessibility, security, database integration, performance, resilience, and readiness gates."
user-invocable: false
---

# Validate Mainframe Modernization

Seek evidence that can falsify the implementation. Do not modify the target or its expected results.

## Procedure

1. Freeze the source revision, target revision, slice plan, contracts, environment, and oracle case set being evaluated.
2. Check bidirectional traceability: every in-scope rule and interface has legacy evidence, a target mapping, and a test; every target behavior has an approved source or decision.
3. Review tests for target-derived expectations, missing boundaries, mock-only claims, weakened assertions, nondeterminism, and absent persisted-state checks.
4. Run applicable domain, API, contract, frontend, accessibility, database integration, end-to-end, security, concurrency, performance, restart, and resilience checks.
5. Compare approved legacy inputs and outcomes with target outputs, errors, database post-state, ordering, audit, side effects, and return behavior.
6. Verify precision, rounding, fixed-width and encoding semantics, null versus blank, dates and time zones, isolation, locking, rollback, retries, and restart behavior.
7. Separate local component evidence, local SQL evidence, full-stack evidence, differential parity, Azure SQL compatibility, and deployment approval.
8. Classify each gate as passed, failed, skipped, or blocked. Never turn an unavailable environment or oracle into a pass.

## Report format

Lead with findings ordered by severity. For each finding include affected rule or interface, legacy evidence, target location, failing or missing test, observable risk, and required disposition. Then report:

- revisions and environment;
- exact commands and actual results;
- oracle coverage and mismatches;
- gate matrix;
- residual risks and approvals;
- the narrowest supported verdict and rollback status.

Compilation, generated tests, mocks, sample UI data, a green port check, and local SQL success are not independent parity or production-readiness evidence.

Use [general verification gates](./references/verification.md) and [full-stack verification guidance](./references/full-stack-verification.md) to select checks and constrain readiness claims.