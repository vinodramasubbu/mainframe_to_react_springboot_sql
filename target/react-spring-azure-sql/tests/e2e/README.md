# TASK-SURV-001 end-to-end tests

The current executable browser check proves only the frontend identity gate fails closed. It is not business or database parity evidence.

Authenticated success, forbidden, not-found, unavailable, timeout, correlation, and browser-to-Azure-SQL oracle cases must be added after the identity provider, sanitized legacy oracle, and seeded SQL environment are approved. Those tests must drive the React task and verify exact values through Spring Boot and the migrated database.