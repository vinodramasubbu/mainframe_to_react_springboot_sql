---
applyTo: "target/react-spring-azure-sql/frontend/**"
---

# React frontend rules

- Use React with TypeScript and the approved, pinned Node.js/package-manager/toolchain baseline.
- Organize by business feature and user task. Keep bootstrap/routing, features, shared UI, and generated API types distinct.
- Consume the approved OpenAPI contract and keep transport DTOs separate from view models when needed.
- Do not connect to Azure SQL, expose database shapes, or import backend implementation types.
- Keep authoritative business rules, state transitions, authorization, and monetary calculations in Spring Boot.
- Treat monetary JSON values as exact strings. Do not use JavaScript `number` for authoritative money, balances, limits, fees, rates, or control totals.
- Handle loading, empty, validation, forbidden, conflict, stale, unavailable, retry, timeout, partial, and unexpected states explicitly.
- Use semantic HTML, programmatic labels, keyboard support, logical focus, accessible error summaries, status announcements, and approved responsive/browser behavior.
- Enforce authorization on the server. Route guards and hidden/disabled controls are usability features only.
- Do not place secrets or client credentials in the browser. Follow the approved PKCE or backend-for-frontend identity design.
- Never log tokens, protected records, or full payment/account data.
- Test components, contracts, accessibility, security, and critical end-to-end task flows. Snapshot tests alone are insufficient.
