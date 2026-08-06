---
description: "Use when creating, running, or reviewing full-stack end-to-end, accessibility, characterization, differential parity, failure, rollback, or restart tests."
applyTo: "target/react-spring-azure-sql/tests/e2e/**"
---

# Full-stack end-to-end test rules

- Drive tests through user-visible React tasks and verify Spring/Azure SQL outcomes against approved characterization cases.
- Use sanitized synthetic or approved masked data only.
- Assert business values, messages, statuses, authorization, ordering, persisted state, audit/correlation, and external effects—not only visual snapshots.
- Cover success, boundary, invalid, unauthorized, conflict, duplicate, unavailable, partial failure, timeout, retry, rollback, and restart cases relevant to the slice.
- Include keyboard, focus, accessible-name, error-announcement, and critical assistive-technology checks.
- Keep tests deterministic. Normalize only explicitly approved nondeterministic fields.
- Record environment, source/target revisions, oracle IDs, commands, results, mismatches, and disposition.
- Do not weaken expected outcomes to make the target pass.
