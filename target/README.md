# Target workspace

This directory starts empty except for this contract. The Implementation Agent creates the approved application slice here only after discovery and planning gates are satisfied.

The repository's selected destination is:

```text
target/react-spring-azure-sql/
  frontend/       React and TypeScript presentation
  backend/        Spring Boot API, application, domain, and adapters
  database/       Migration support, local development, and reconciliation assets
  tests/e2e/      Integrated browser tests
```

Do not add a generic scaffold before an approved slice defines runtime versions, contracts, identity, data mappings, tests, and operational requirements. Generated target code must remain traceable to `modernization/` evidence and plans.
