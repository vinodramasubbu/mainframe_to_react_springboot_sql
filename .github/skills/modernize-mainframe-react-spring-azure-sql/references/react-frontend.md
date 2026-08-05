# React frontend rules

Build an accessible TypeScript frontend that expresses user tasks and consumes approved Spring Boot contracts. Keep authoritative business behavior on the server.

## Structure

Prefer a feature-oriented layout such as:

```text
frontend/
  src/
    app/                    bootstrap, routing, providers
    features/               bounded user tasks
    shared/                 approved reusable UI only
    generated/api/          generated or contract-derived API types
    test/                   shared test support
```

Select and pin the React build tool, router, form library, query/cache library, component system, test tools, and package manager through the project ADR. Do not introduce a second tool for the same responsibility.

## Contract and state

- Generate or derive API types from the approved OpenAPI contract. Do not hand-maintain duplicate DTO models without a documented reason.
- Keep transport DTOs separate from view models where presentation needs differ.
- Represent exact monetary values as strings at the JSON boundary. Format for display without performing authoritative arithmetic in JavaScript.
- Represent dates and times as documented ISO strings. Do not let browser locale silently change business dates or time zones.
- Make server state, URL state, form state, and local presentation state explicit. Do not copy server state into multiple stores.
- Use optimistic UI only when idempotency, concurrency, failure restoration, and user feedback are designed and tested.
- Handle loading, empty, partial, stale, forbidden, validation, conflict, unavailable, retry, and unexpected states.
- Preserve correlation identifiers and safe error codes without displaying stack traces or internal details.

## Validation and business rules

- Use client validation for usability and immediate feedback.
- Repeat all security, integrity, state-transition, and authoritative validation in Spring Boot.
- Do not calculate approved payment totals, limits, fees, balances, eligibility, settlement outcomes, or status transitions in React.
- Do not hide invalid operations solely through UI state. The API must enforce allowed actions.

## Security

- Choose direct SPA bearer-token handling versus a backend-for-frontend through the threat model and identity ADR.
- Use authorization-code flow with PKCE when a direct SPA identity flow is approved.
- Do not store client secrets in the frontend.
- Avoid persistent browser token storage unless the approved identity design explicitly requires and mitigates it.
- Configure CORS narrowly. Apply CSRF protection when cookie-based authentication or a BFF makes it relevant.
- Encode output by default; sanitize only approved rich content.
- Never log tokens, protected records, full payment/account values, or sensitive API bodies.
- Treat route guards and hidden controls as usability only; enforce authorization in Spring Boot.

## Accessibility and usability

- Use semantic HTML and native controls before custom widgets.
- Provide programmatic labels, descriptions, error association, headings, landmarks, and logical focus order.
- Support keyboard-only task completion and visible focus.
- Announce validation summaries, async failures, status changes, and completion results appropriately.
- Do not rely on color, position, or icons alone for meaning.
- Preserve significant formatting while keeping the accessible name and underlying value clear.
- Test zoom, reflow, supported browsers, screen-reader paths, timeouts, and error recovery according to approved requirements.

## Testing

Require:

- Unit tests for presentation-only transformations.
- Component tests for fields, messages, actions, permissions, loading, errors, and boundary values.
- Contract tests against the approved OpenAPI schema.
- Accessibility automation plus manual keyboard and assistive-technology checks for critical tasks.
- End-to-end tests through React, Spring Boot, and Azure SQL for approved oracle cases.
- Security checks for dependency, token, XSS, CSRF/CORS, authorization, and protected-data exposure risks.

Snapshot tests alone do not prove behavior or accessibility. Prefer assertions on roles, labels, values, messages, actions, API effects, and focus.
