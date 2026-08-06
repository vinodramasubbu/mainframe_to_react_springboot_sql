# Three-tier target architecture

## Fixed platform

- React with TypeScript owns presentation and browser interaction.
- Spring Boot with plain Java domain rules owns application behavior.
- Azure SQL Database owns converted relational persistence.

Architecture ADRs decide slice-specific modules, persistence, transactions, integration, security, deployment, and coexistence. They do not reopen the implementation stack.

## Tier responsibilities

### Presentation and API

- React communicates only through approved HTTP/event contracts and never accesses Azure SQL or mainframe protocols.
- API adapters own protocol mapping, authentication context, boundary validation, versioning, rate controls, and safe error contracts.
- Controllers and endpoints contain no business calculations or persistence behavior.

### Application and domain

- Application services own use-case orchestration, authorization policy, idempotency, and transaction demarcation.
- Dependency-free domain types and rules own calculations, validation, state transitions, money, dates, identifiers, quantities, and statuses.
- Domain code must not depend on React, HTTP, Spring, ORM, Azure SDK, queue clients, or filesystems.

### Data and integration

- Adapters own Azure SQL access, legacy coexistence, MQ/events, files, external services, caching, technical retries, and vendor-to-domain mapping.
- Timeouts, retry limits, pools, circuit behavior, and telemetry must not change business outcomes.

Dependencies point from outer adapters toward application/domain abstractions and cross tier boundaries through explicit contracts.

## Deployment boundaries

Prefer a modular monolith when capabilities share transactions/data or independent scaling and release cadence are not evidenced. Split services only with a bounded context, independent ownership/deployment, explicit data ownership, and designed consistency. Never create one service per legacy program or table.

## Architecture gate

Approve before implementation:

- Context, tier/module/deployment views, dependency rules, and ownership.
- Persistence, transaction, batch, integration, identity, observability, hosting, and coexistence ADRs.
- API/event/file contracts and compatibility policy.
- Data ownership, source-to-target mapping, migration, reconciliation, and recovery.
- Threat model, nonfunctional criteria, accessibility, privacy, records, and audit requirements.
- Build reproducibility, dependency governance, SBOM/scanning, deployment promotion, feature controls, cutover, and rollback.
- Traceability from every target component and contract to approved evidence, rule, interface, task, and test IDs.