# Modernization deliverables

Create only artifacts relevant to the current phase, using these canonical locations.

## Inventory

`modernization/inventory/`

- `extraction-manifest.csv`: original system/revision, dataset/member or USS path, local path, attributes, encodings, transfer mode, hash, status, exclusion approval.
- `artifact-inventory.json` and `.csv`: deterministic local inventory from the bundled script.
- `reconciliation.md`: expected, extracted, excluded, failed, and resolved counts with owner approval.

## Analysis

`modernization/analysis/`

- `system-context.md`: actors, capabilities, entry points, external systems, trust boundaries.
- `dependency-graph.mmd`: stable node/edge IDs and evidence.
- `entry-points.csv`: trigger, contract/layout, program/job, authorization, outcome.
- `business-rules.csv`: rule ID, trigger, inputs, rule, effects, exceptions, transaction, evidence, confidence, owner.
- `data-dictionary.csv`: field, copybook/schema source, type, precision/scale, encoding, allowed values, sensitivity, target type.
- `unknowns-and-risks.csv`: ID, gap, affected scope, consequence, evidence needed, owner, due date, status.

## Characterization evidence

`modernization/evidence/`

- `characterization/`: approved legacy cases and provenance without protected data.
- `characterization/case-index.csv`: case ID, rule/interface IDs, source revision, sanitized input, expected observable output, normalization, tolerance, owner, and approval.
- `characterization/README.md`: execution prerequisites, authoritative environment, capture procedure, protected-data controls, and known limitations.

## Modernization plan

`modernization/plan/`

- `modernization-roadmap.md`: application scope, evidence baseline, phase sequence, slice order, gates, owners, expected outputs, completion criteria, and revision history.
- `phase-dag.json`: machine-readable dependency graph used by the coordinator to select the next unblocked work node.
- `status.md`: append-only execution ledger containing node transitions, evidence links, commands and actual results, blockers, approvals, decisions, and next-node selection.

Each DAG node must contain:

```text
id: stable node ID
kind: milestone | work | approval
phase: discovery | handoff | approval | design | implementation | validation | coexistence | rollback | cutover | decommission
sliceId: bounded capability ID or null for application-wide work
parentId: containing milestone ID or null
title: concise work outcome
dependsOn: predecessor node IDs
requiredInputs: evidence or approved artifacts
expectedOutputs: artifacts and executable checks
readinessCriteria: conditions checked before planned becomes ready
completionCriteria: objective checks required before in-progress becomes completed
owner: accountable role
status: planned | ready | in-progress | blocked | completed | superseded
supersededBy: replacement node IDs when status is superseded
expansionRevision: approved expansion-manifest revision for milestone nodes or null
expansionDigest: SHA-256 of the canonical expansion manifest for milestone nodes or null
```

Use this top-level JSON shape:

```json
{
	"schemaVersion": 1,
	"application": "<application-id>",
	"roadmapRevision": 1,
	"sourceRevision": "<immutable evidence revision>",
	"updatedAt": "<ISO-8601 timestamp>",
	"nodes": []
}
```

Keep node IDs stable across revisions. Store dependency IDs in `dependsOn`; do not encode sequencing only through array order. An executable node may become `ready` only when every predecessor is `completed` and its readiness criteria are satisfied. It becomes `completed` only when its completion criteria pass.

Use `milestone` nodes as stable containers for coarse phases discovered before target design. Milestones are not executed. After handoff approval, the implementation skill adds `work` or `approval` children under the existing milestone using `parentId`; downstream phases continue to depend on the milestone. A milestone becomes `completed` only when all non-superseded children are completed. This expands the graph without rewiring external dependencies or deleting history.

Expand each milestone deterministically and idempotently from an approved expansion manifest stored beside the target work packets:

- Use only these stage keys: `design`, `contract`, `domain`, `database`, `api`, `frontend`, `integration`, `validation`, `parity`, `coexistence`, `rollback`, `cutover`, and `decommission`.
- Give each manifest item a stable, unique, lower-case kebab-case `outcomeKey` derived from its approved outcome. Derive its child ID as `<milestone-id>.<stage>.<outcomeKey>`.
- The manifest explicitly lists each child's `dependsOn`. Entry children inherit the milestone's own predecessors; internal children name their direct child prerequisites. Successor milestones remain dependent on the parent milestone.
- Canonicalize the manifest by sorting items by derived child ID and object keys lexicographically, serialize as UTF-8 JSON with LF endings and no insignificant whitespace, and store its SHA-256 as the milestone's `expansionDigest` with its `expansionRevision`.
- On resume, the same revision and digest must produce the same IDs and edges. Reuse matching children; do not append duplicates. A changed digest requires a new approved expansion revision.
- If a derived ID collides, an existing child differs, or the manifest has a missing/cyclic edge, mark expansion blocked and record a roadmap revision decision. Never silently overwrite or rename executed nodes.
- Derive the milestone status from its non-superseded children. An unexpanded milestone remains `planned`; it is never selected as executable work.

Represent every human decision as an `approval` node. Its readiness criteria require a reviewable package; its completion criteria require a structured approval record. Do not put owner approval into a work node's readiness criteria. An `approved-with-conditions` decision completes the node only when each condition is represented as a required input, constraint, or follow-up node in the DAG.

`phase-dag.json` is authoritative for graph structure. Node `status` values are a cache derived by replaying `status.md`; the append-only ledger is authoritative for transition history.

Every `status.md` transition is one fenced JSON object with these required fields:

```text
sequence: unique integer, exactly previous sequence + 1
transitionId: unique stable ID
timestamp: ISO-8601 UTC timestamp
roadmapRevision: integer
nodeId: existing DAG node ID
from: prior derived status
to: allowed next status
actorRole: accountable role
actorIdentity: person or agent identity
reason: concise transition reason
evidence: artifact paths, approval record, or command results
correctsTransitionId: earlier transition ID or null
```

Allowed transitions are `planned -> ready|blocked|superseded`, `ready -> in-progress|blocked|superseded`, `in-progress -> completed|blocked|superseded`, `blocked -> planned|ready|superseded`, and `completed -> superseded`. A correction never edits history: append a transition referencing `correctsTransitionId`, then replay in sequence order. Duplicate or missing sequences, duplicate transition IDs, invalid edges, malformed records, roadmap-revision conflicts, or competing transitions from the same state block execution for owner reconciliation. Write and validate the ledger transition first, then update the DAG cache. On cache disagreement, replay the ledger before selecting another node.

The DAG is evidence-backed sequencing, not permission to cross a human gate. Discovery may identify target-phase milestones and dependencies, but it must not invent target architecture, contracts, schema, or implementation tasks. The implementation skill expands approved milestones with child nodes and records each DAG revision without deleting execution history.

## Approved handoff

`modernization/handoff/`

- `slice-candidates.csv`: capability, entry points, dependencies, risk, evidence readiness, oracle coverage, and proposed order.
- `<slice-id>.md`: the approved evidence package consumed by the target implementation skill.

Every handoff must state:

```text
Capability/slice:
Entry points:
Legacy artifacts and revision:
Business-rule IDs:
Interfaces and data:
Test/oracle IDs:
Approved normalization/tolerances:
Transaction/restart/external effects:
Nonfunctional constraints:
Unknowns and blocked behavior:
Evidence commands and actual results:
Business/mainframe/data owner approvals:
```

Record each approval as:

```text
Approval node ID:
Accountable role:
Approver identity:
Decision: approved | approved-with-conditions | rejected
Decision timestamp:
Approved scope/slice:
Source revision:
Handoff revision:
Roadmap revision:
Conditions and expiry:
Evidence/reference:
```

This approval record is canonical for handoff, design, contract, security, data, rollback, cutover, and decommission decisions. Target work packets must embed or reference the complete record; a free-text approval note is insufficient.

Validate approval scope, conditions, expiry, and source/handoff/roadmap revisions on every resume and before promoting a successor. When an approval expires or its approved revision changes, append `completed -> superseded` for that approval node. Move its `ready` or `in-progress` successors to `blocked`; leave unstarted successors `planned`. If dependent work is already completed, add a blocking impact-assessment and reapproval node before any later phase can proceed. Never retain authorization solely because a predecessor was once completed.

The discovery roadmap and phase DAG own cross-phase sequencing and approval dependencies. Target architecture, contracts, Azure SQL mapping and DDL, ADRs, detailed implementation work packets, parity evidence, cutover, rollback, and decommission artifacts are owned by `modernize-mainframe-react-spring-azure-sql` after the relevant handoff is approved.
