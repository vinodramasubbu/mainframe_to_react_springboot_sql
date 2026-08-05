---
applyTo: "legacy-source/**"
---

# Legacy-source preservation

- Treat these files as read-only forensic evidence.
- Never edit, format, rename, move, regenerate, or delete them.
- Preserve original path, dataset/member identity, case, line endings, fixed columns, sequence fields, trailing spaces, encoding metadata, RECFM, and LRECL.
- Put annotations, recovered rules, dependency mappings, and normalized views under `modernization/`, not beside the source.
- Cite path plus line/statement range in analysis. For generated or expanded source, cite both generated output and its original template/copybook when known.
- If a file is corrupt, truncated, empty, binary, incorrectly decoded, or missing a dependency, record the issue and request a verified re-extraction. Do not repair evidence in place.
