# How to modernize with GitHub Copilot

This guide explains how to plan, implement, and validate a COBOL/CICS/Db2 to
**React + Spring Boot + Azure SQL Database** modernization using the GitHub
Copilot instructions and skills checked into `.github/`.

You describe *what capability* to modernize; Copilot follows the repo's governance files to do it the same, safe, evidence-backed way every time.

---

## 1. How the `.github` customization is wired

Copilot automatically loads three kinds of files. You do not attach them manually.

| Layer | Location | When it applies | Purpose |
|---|---|---|---|
| **Repo instructions** | `.github/copilot-instructions.md` | Always | The non-negotiable rules: evidence-first, bounded slices, Azure SQL target, decimal/precision fidelity, security, "never claim done while gaps remain". |
| **Path instructions** | `.github/instructions/*.instructions.md` | Auto, when you touch a matching path (`applyTo` glob) | Stack-specific rules for the frontend, backend, database, local SQL testing, e2e, legacy source. |
| **Skills** | `.github/skills/*/SKILL.md` | When your request matches the skill's description | The full step-by-step modernization workflow (analysis → oracle → design → slice → verify → cutover). |

### Instruction files (path-scoped, auto-applied)
| File | Applies to |
|---|---|
| `legacy-source.instructions.md` | `legacy-source/**` — treat COBOL/JCL/copybooks as **immutable evidence** |
| `react-frontend.instructions.md` | `target/react-spring-azure-sql/frontend/**` |
| `react-spring-backend.instructions.md` | `target/react-spring-azure-sql/backend/**` |
| `react-spring-database.instructions.md` | `target/.../database/**`, `.../db/migration/**` |
| `react-spring-local-sql.instructions.md` | `.../database/local/**`, `.../backend/src/test/**` |
| `react-spring-e2e.instructions.md` | `target/react-spring-azure-sql/tests/e2e/**` |
| `react-spring-stack.instructions.md` | `target/react-spring-azure-sql/**` (whole stack) |

### Skills (intent-matched)
| Skill | Use when |
|---|---|
| `modernize-mainframe-react-spring-azure-sql` | You are building the **React + Spring Boot + Azure SQL** slice (this repo's chosen stack). |
| `modernize-mainframe-application` | Platform-neutral extraction, inventory, and source discovery only |

For the current implementation, use `modernize-mainframe-react-spring-azure-sql`.
Use the platform-neutral skill only for extraction, inventory, or source
discovery. All planning, architecture, implementation, and validation in this
repository use `modernize-mainframe-react-spring-azure-sql`.

---

## 2. Repository layout

```
legacy-source/                     # Immutable mainframe evidence (COBOL, JCL, copybooks, DDL, BMS)
  DEV1/{BANKDEMO,SURVDEMO,TRSYDEMO}/{COBOL,COPY,JCL,DDL,BMS,...}
modernization/react-spring-azure-sql/
  analysis/                        # Recovered behavior and risks
  architecture/                    # ADRs, OpenAPI, and data mappings
  evidence/                        # Actual validation results and blocked gates
  plans/                           # Reviewed bounded-slice plans
target/react-spring-azure-sql/     # The modernized system
  frontend/                        # React + TypeScript (Vite)
  backend/                         # Spring Boot + JDBC + Flyway
    src/main/resources/db/migration/  # V1, V2, ... Azure SQL migrations
  database/local/                  # Guarded local SQL Server and sample-data scripts
.github/
  copilot-instructions.md
  instructions/*.instructions.md
  skills/*/SKILL.md
```

---

## 3. The modernization workflow (what the skill enforces)

Every capability is delivered as **one bounded vertical slice**, never a whole-app translation. For each slice Copilot will:

1. **Reconcile the source boundary** — read the exact COBOL program(s), copybooks, DDL, JCL, and characterization fixtures. Never invent missing copybooks/schemas.
2. **Recover behavior** — extract business rules, each with a **stable rule ID**, a **legacy evidence citation**, and a **target mapping**.
3. **Bind the oracle** — use exact expected values from approved legacy
  characterization evidence. If no independent oracle exists, record parity as
  blocked; generated target tests are not a substitute.
4. **Implement pure rules first** — dependency-free Java domain classes (no HTTP/DB/vendor types), with one test per rule and the fixture bound as exact expectations.
5. **Add persistence + orchestration** — Azure SQL migration, JDBC repository, transactions, and preserved return-code/rollback semantics.
6. **Add the endpoint** — REST controller + error contract.
7. **Add the UI** — accessible React workflow that calls the backend only (no duplicated business rules).
8. **Verify** — run the applicable unit, contract, component, accessibility,
   database, browser, security, and differential checks. Report each result or
   blocker separately.

**Golden rule:** a missing column, wrong schema qualifier, or narrowed type shows up only as a generic 5xx — so schema must stay consistent across migration, seed, and query, and be diffed against the full legacy DDL.

---

## 4. Create and review a modernization plan

Planning is separate from implementation. Start with an application boundary and
ask Copilot to produce evidence-backed artifacts without generating target code.

```text
Use the modernize-mainframe-react-spring-azure-sql skill.

Analyze SURVDEMO and create an evidence-backed modernization plan. Treat
legacy-source as immutable and do not implement yet.

Inventory entry points, dependencies, screens, business rules, data, transactions,
failure behavior, security, batch/restart behavior, external interfaces, unknowns,
and risks. Give every rule a stable ID and precise legacy citation.

Select the smallest useful first vertical slice. Define its scope and exclusions,
rule/UI/API/data traceability, OpenAPI and error contracts, Azure SQL mapping,
approved oracle cases or evidence gaps, implementation sequence, rollback, and
acceptance gates. Stop where missing evidence could change behavior.
```

Before implementation, review that the plan:

- Names a bounded task ID, entry point, programs, copybooks, data, and interfaces.
- Gives every in-scope rule an evidence citation and exact expected outcomes.
- Defines UI, OpenAPI, error, identity, transaction, and database contracts.
- Preserves precision, fixed-width, null/blank, ordering, locking, rollback,
  restart, return-code, audit, and side-effect behavior.
- Lists assumptions, missing evidence, owners, rollback, and approval gates.
- Excludes adjacent programs and batch paths that are not part of the task.

Do not accept a plan that treats compilation or generated unit tests as legacy
parity evidence.

## 5. Execute one bounded slice

Name the approved plan task explicitly. A useful execution prompt is:

```text
Use the modernize-mainframe-react-spring-azure-sql skill.

Implement only TASK-SURV-001 from the reviewed SURVDEMO plan as one bounded
vertical slice. Treat legacy-source as immutable and do not expand into adjacent
programs or batch behavior.

Implement and validate in this order:
1. Characterization-bound tests and dependency-free Java domain rules.
2. Reviewed Azure SQL migration, parameterized repository, and transactions.
3. Application orchestration, endpoint, authorization, and safe error handling.
4. Accessible React task generated from the approved OpenAPI contract.
5. Database integration and browser checks appropriate to the available environment.
6. Traceability, risks, actual validation evidence, and rollback notes.

After each substantive edit, run the cheapest check that can falsify the change.
Report commands, pass/fail/skip counts, mismatches, assumptions, and blocked gates.
Do not claim parity without an approved independent legacy oracle.
```

Effective prompts name the application, task ID, plan path, evidence, expected
observable behavior, exclusions, and required validation. Avoid prompts such as
“convert the whole estate to Java” or “make all tests pass”; they do not define a
reviewable behavior boundary or acceptance oracle.

---

## 6. Local development quick start

Prerequisites are Docker Desktop with a running engine, a JDK capable of compiling
Java 17 bytecode, Maven 3.9, Node.js 22.12 or newer, and npm. Host `sqlcmd` is not
required by the guarded scripts.

Create an ignored local environment file from
`target/react-spring-azure-sql/database/local/.env.example`, then initialize SQL
Server and the empty `SURVDEMO` database:

```powershell
cd target/react-spring-azure-sql/database/local
Copy-Item .env.example .env
# Edit .env locally and set a strong development-only password.
./initialize.ps1
```

Configure the backend environment exactly as documented in
`target/react-spring-azure-sql/database/local/README.md`, then start Spring Boot so
Flyway applies the authoritative migration chain:

```powershell
cd ../../backend
mvn spring-boot:run
```

In another terminal, load the deterministic sanitized samples:

```powershell
cd target/react-spring-azure-sql/database/local
./seed.ps1
```

Start the frontend:

```powershell
cd target/react-spring-azure-sql/frontend
npm install
npm run dev
```

Live API access remains secured and requires the approved JWT issuer and
`SCOPE_survivor.inquiry`. Until identity is configured, the UI can be explored at:

```text
http://127.0.0.1:5173/?sampleData=true
```

Sample mode is development-only and returns fixed contract fixtures. It does not
call Spring Boot or SQL Server and is not full-stack, authentication, database, or
parity evidence.

---

## 7. Test and validate the converted application

Each validation layer proves something different. Passing one does not substitute
for another.

| Layer | What it proves | Checks |
|---|---|---|
| Domain/unit | Recovered rules match exact cases | Java tests for rules, boundaries, precedence, and rounding |
| API/contract | Validation, authorization, errors, and DTOs match | Spring MVC/security tests and OpenAPI generation |
| Frontend/component | Task states and accessibility work | Vitest, Testing Library, axe, ESLint, TypeScript/Vite build |
| Database integration | Real migrations, SQL, mappings, and constraints work | Testcontainers or a disposable local SQL Server |
| Full-stack E2E | Browser to Spring to migrated database works | Playwright through the secured API |
| Differential parity | Target behavior matches approved legacy outcomes | Exact output and persisted post-state comparisons |
| Azure readiness | Deployed Azure SQL behavior is acceptable | Clean migration, schema, plans, concurrency, security, and recovery |

Run backend checks from their project directory:

```powershell
cd target/react-spring-azure-sql/backend
mvn test
mvn package
```

Run frontend checks:

```powershell
cd target/react-spring-azure-sql/frontend
npm install
npm run lint
npm test
npm run build
```

Run browser checks:

```powershell
cd target/react-spring-azure-sql/tests/e2e
npm install
npx playwright install chromium
npm test
```

A skipped Testcontainers test must be reported as skipped, not counted as database
validation. Do not replace SQL Server with H2 to obtain a passing build. Local SQL
success is preliminary; required checks must still run against Azure SQL Database.

Use this validation prompt:

```text
Use the modernize-mainframe-react-spring-azure-sql skill.

Validate TASK-SURV-001 without weakening expected outcomes. Run the relevant
domain, API/security, frontend, accessibility, clean migration/repository, and
browser checks. Compare every available approved oracle case and persisted
post-state.

Write a validation report with the source and target revisions, environment,
oracle IDs, exact commands, pass/fail/skip counts, migration result, mismatches,
blocked identity/Azure SQL/parity/performance/recovery gates, rollback status, and
the narrowest evidence-supported readiness verdict. Do not infer readiness from
unit tests or development sample mode.
```

## 8. Definition of done for a slice

A slice is only "done" when all of these hold — Copilot must not claim parity otherwise:

- [ ] Every recovered rule has an ID, a legacy citation, a target mapping, and at least one test.
- [ ] Characterization/parity values are bound as **exact** expectations (ids, `HALF_UP` scale-2 rounding, version transitions, return codes).
- [ ] Azure SQL migration is additive, reviewable, and schema-consistent with seeds and queries.
- [ ] Transaction/return-code semantics match the legacy program (clean / business-exception / technical-failure + rollback).
- [ ] Unexpected-exception handlers log the cause + correlation id (never swallow it).
- [ ] Persisted DB state is diffed against the approved post-state fixture through the real endpoint.
- [ ] Precision, encoding, null/blank, ordering, and restart behavior are preserved.
- [ ] Static, unit, contract, component, accessibility, database, E2E, security,
  and parity checks required by the slice have actual recorded results.
- [ ] Azure SQL compatibility, rollback, reconciliation, residual risks, and
  approval owners are recorded.

“Component-validated,” “database-tested,” “parity-verified,” and
“deployment-approved” are different statuses. Use only the narrowest status
supported by the evidence.

## 9. Current status (SURVDEMO)

| Capability | Legacy program | Evidence-supported state |
|---|---|---|
| Survivor inquiry (`TASK-SURV-001`) | `SURVINQ` | Implemented; component tests and local SQL validation recorded |
| Entitlement validation | `SURVVALID` | Legacy evidence only; not implemented as a separate target slice |
| Monthly benefit batch | `SURVCALC` | Legacy evidence only; not implemented or parity-verified |

TASK-SURV-001 is not yet authenticated full-stack E2E approved, legacy
differential-parity approved, Azure SQL environment validated, or deployment
approved. See
`modernization/react-spring-azure-sql/evidence/task-surv-001-validation.md` for
the recorded checks and blocked gates. `BANKDEMO` and `TRSYDEMO` remain legacy
evidence awaiting their own approved slices.

---

## 10. Prompt library

### Add deterministic sample data

```text
Give me sanitized deterministic sample data to test TASK-SURV-001 locally. Cover
every approved visible outcome and a documented not-found pair. Load it only after
Flyway migration, preserve exact decimal scales and constraints, reserve clear
sample IDs, and make reruns replace only those IDs. Keep it outside production
migrations and do not add an authentication bypass.
```

### Investigate an integration failure

```text
The inquiry returns a generic 5xx. Trace the request through the controller,
application service, JDBC repository, and actual migrated schema. Check schema
qualifiers, missing columns, fixed-width identifiers, constraints, and seed/query
consistency. Preserve the error contract, log the cause with its correlation ID,
make the smallest evidence-backed fix, and rerun the failing check.
```

### Validate completeness

```text
Run completeness validation for TASK-SURV-001. Verify rule traceability, OpenAPI
compatibility, exact values, migration chain, repository SQL, browser states,
accessibility, authorization, correlation handling, rollback, and approved parity
cases. List every skipped or blocked gate and do not infer readiness from a build.
```

## 11. Editing the governance files

- Keep `.instructions.md` frontmatter valid: a quoted `applyTo` glob (comma-separated for multiple paths).
- Keep each `SKILL.md` frontmatter's `name` equal to its folder name (lowercase-hyphenated) with a `description`.
- Record durable lessons in the instructions/skill so future runs benefit (this repo already encodes the schema-drift, parity-binding, and observability lessons).
