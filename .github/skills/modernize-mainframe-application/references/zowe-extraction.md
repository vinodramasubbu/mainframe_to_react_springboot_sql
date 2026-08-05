# Zowe extraction and reconciliation

## Contents

1. Source boundary
2. Transfer strategy
3. Required metadata
4. Coverage reconciliation
5. Failure conditions

## Source boundary

Obtain authoritative library and version information from the source-management system and application owners before using broad dataset patterns. Include:

- COBOL, PL/I, Assembler, Java, REXX, CLIST, and generated sources.
- Copybooks, macros, includes, BMS/MFS maps, DBDs, PSBs, DDL, SQL, bind cards, and stored procedures.
- JCL, PROCs, scheduler definitions, SORT/control cards, IDCAMS, utilities, and deployment jobs.
- CICS, IMS, MQ, Db2, VSAM, security, environment, feature-flag, and operational definitions.
- USS application directories, scripts, configuration, certificates metadata, and build definitions. Never extract private keys or secrets into the repository.
- Interface schemas, file layouts, reports, accepted test cases, runbooks, and source-management metadata.

Do not treat load modules as recoverable source. If source is missing, record that fact and use disassembly or behavioral observation only through an approved specialist process.

## Transfer strategy

Use Zowe CLI or Zowe Explorer with an authorized z/OS identity. Examples:

```text
zowe zos-files download all-members "HLQ.APP.COBOL" --directory legacy-source/HLQ/APP/COBOL --extension .cbl --fail-fast false

zowe zos-files download data-sets-matching "HLQ.APP.**" --directory legacy-source --extension-map cobol=cbl,copy=cpy,jcl=jcl,proc=jcl --fail-fast false

zowe zos-files download uss-directory "/u/app" --file legacy-source/uss/app
```

Check command help for the installed Zowe CLI version before automation.

Important limitations:

- The bulk `data-sets-matching` flow covers matching physical sequential and partitioned datasets; it is not an all-z/OS extractor.
- Extract USS trees separately and deliberately handle symlinks, permissions, tags, encodings, and binary files.
- Export VSAM/IMS data only with an approved layout and privacy-safe sampling strategy. Preserve key, order, status, and record metadata.
- Treat source-manager projects, packages, stages, variants, and processor metadata as part of the source of truth.
- Use binary/record transfer where translation would corrupt artifacts. Use deliberate EBCDIC-to-local conversion for text and record the source and destination encodings.
- Never commit production data, credentials, tokens, certificates with private material, or unmasked protected information.

## Required metadata

Create a transfer manifest with one row per dataset/member or USS file:

| Field | Requirement |
|---|---|
| `source_system` | LPAR/sysplex and environment identifier |
| `source_revision` | SCM project/package/stage/version or extraction timestamp |
| `original_name` | Dataset(member) or absolute USS path |
| `local_path` | Repository-relative path |
| `artifact_type` | Source, copybook, JCL, schema, map, config, data sample, binary, other |
| `dsorg_recfm_lrecl` | Dataset organization and record attributes where applicable |
| `source_encoding` | CCSID/code page or binary/record |
| `local_encoding` | Resulting local encoding |
| `transfer_mode` | Text, binary, or record |
| `size_and_hash` | Byte count and SHA-256 after extraction |
| `result` | Success, warning, failure, or intentionally excluded |
| `exclusion_owner_reason` | Approval and rationale for exclusions |

Store commands and complete logs without secrets.

## Coverage reconciliation

1. Inventory source-manager elements, cataloged datasets, PDS/PDSE members, USS files, and external definitions.
2. Compare expected and downloaded counts by source container and artifact type.
3. Compare manifest paths with the deterministic local inventory.
4. Resolve duplicate names, empty members, unreadable files, missing copybooks, aliases, generations, dynamic includes, and failed transfers.
5. Sample decoded files from every encoding and record format. Verify fixed columns, special characters, trailing spaces, and line counts.
6. Obtain owner acceptance for every intentional exclusion.

Extraction passes only when expected equals downloaded plus explicitly approved exclusions and every transfer failure is resolved.

## Failure conditions

Stop affected analysis when:

- Encoding, record format, or source revision is unknown.
- A referenced copybook, macro, PROC, schema, map, or generated source is missing.
- A source-management export conflicts with a raw dataset copy.
- A file appears truncated, corrupted, silently normalized, or transferred in the wrong mode.
- Access controls prevent complete inventory of the approved boundary.
