#!/usr/bin/env python3
"""Create a deterministic, content-free inventory of extracted source files."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
import sys
from collections import Counter
from pathlib import Path
from typing import Any


DEFAULT_EXCLUDED_DIRS = {
    ".git",
    ".idea",
    ".vscode",
}

FILE_ATTRIBUTE_REPARSE_POINT = 0x400

CATEGORIES = {
    ".asm": "assembler",
    ".bms": "cics-map",
    ".cbl": "cobol",
    ".cob": "cobol",
    ".cpy": "copybook",
    ".copy": "copybook",
    ".copybook": "copybook",
    ".cs": "csharp",
    ".ctl": "control-card",
    ".dbd": "ims-dbd",
    ".ddl": "database-schema",
    ".java": "java",
    ".jcl": "jcl",
    ".json": "structured-data",
    ".mac": "assembler-macro",
    ".mfs": "ims-map",
    ".pli": "pli",
    ".pl1": "pli",
    ".proc": "jcl-proc",
    ".psb": "ims-psb",
    ".rexx": "rexx",
    ".rex": "rexx",
    ".sql": "sql",
    ".xml": "structured-data",
    ".yaml": "configuration",
    ".yml": "configuration",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Inventory extracted files without copying their content into the manifest. "
            "Paths are sorted and hashes are SHA-256."
        )
    )
    parser.add_argument("root", type=Path, help="Root directory containing extracted artifacts")
    parser.add_argument("--output", type=Path, required=True, help="JSON manifest path")
    parser.add_argument("--csv", type=Path, help="Optional flat CSV manifest path")
    parser.add_argument(
        "--exclude-dir",
        action="append",
        default=[],
        help="Additional directory name to exclude; repeat as needed",
    )
    parser.add_argument(
        "--large-file-bytes",
        type=int,
        default=10 * 1024 * 1024,
        help="Flag files at or above this size (default: 10 MiB)",
    )
    return parser.parse_args()


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def sha256_text(value: str) -> str:
    return hashlib.sha256(value.encode("utf-8", errors="surrogateescape")).hexdigest()


def is_link_like(path: Path) -> bool:
    if path.is_symlink():
        return True
    attributes = getattr(path.lstat(), "st_file_attributes", 0)
    return bool(attributes & FILE_ATTRIBUTE_REPARSE_POINT)


def inspect_text(path: Path) -> tuple[bool, str | None, int | None]:
    sample = path.read_bytes()[:65536]
    if b"\x00" in sample:
        return True, None, None

    for encoding in ("utf-8", "utf-8-sig"):
        try:
            sample.decode(encoding)
            with path.open("r", encoding=encoding, errors="strict", newline="") as stream:
                return False, encoding, sum(1 for _ in stream)
        except UnicodeError:
            continue

    return True, None, None


def category_for(path: Path, binary: bool) -> str:
    if binary:
        return "binary-or-undecoded"
    return CATEGORIES.get(path.suffix.lower(), "unknown-text")


def iter_files(
    root: Path,
    excluded_dirs: set[str],
    excluded_paths: set[Path],
) -> list[Path]:
    result: list[Path] = []
    for current_root, dirs, files in os.walk(root):
        current = Path(current_root)
        traversable_dirs: list[str] = []
        for name in sorted(dirs):
            if name in excluded_dirs:
                continue
            path = current / name
            if is_link_like(path):
                if path not in excluded_paths:
                    result.append(path)
            else:
                traversable_dirs.append(name)
        dirs[:] = traversable_dirs
        for name in sorted(files):
            path = current / name
            if path not in excluded_paths:
                result.append(path)
    return sorted(result, key=lambda item: item.relative_to(root).as_posix())


def build_record(root: Path, path: Path, large_file_bytes: int) -> dict[str, Any]:
    if is_link_like(path):
        target = os.readlink(path)
        return {
            "relative_path": path.relative_to(root).as_posix(),
            "entry_type": "symbolic-link",
            "symlink_target": target,
            "size_bytes": path.lstat().st_size,
            "sha256": sha256_text(target),
            "extension": path.suffix.lower(),
            "category": "symbolic-link",
            "binary_or_undecoded": False,
            "detected_local_encoding": None,
            "line_count": None,
            "flags": ["symbolic-link"],
        }

    stat = path.stat()
    binary, encoding, line_count = inspect_text(path)
    flags: list[str] = []
    if stat.st_size == 0:
        flags.append("empty")
    if stat.st_size >= large_file_bytes:
        flags.append("large")
    if binary:
        flags.append("binary-or-undecoded")
    if not path.suffix:
        flags.append("no-extension")

    return {
        "relative_path": path.relative_to(root).as_posix(),
        "entry_type": "file",
        "symlink_target": None,
        "size_bytes": stat.st_size,
        "sha256": sha256_file(path),
        "extension": path.suffix.lower(),
        "category": category_for(path, binary),
        "binary_or_undecoded": binary,
        "detected_local_encoding": encoding,
        "line_count": line_count,
        "flags": flags,
    }


def write_json(
    output: Path,
    root: Path,
    records: list[dict[str, Any]],
    excluded_dirs: set[str],
) -> None:
    categories = Counter(record["category"] for record in records)
    flags = Counter(flag for record in records for flag in record["flags"])
    payload = {
        "schema_version": 2,
        "inventory_root": str(root),
        "file_count": len(records),
        "total_bytes": sum(record["size_bytes"] for record in records),
        "category_counts": dict(sorted(categories.items())),
        "flag_counts": dict(sorted(flags.items())),
        "excluded_directory_names": sorted(excluded_dirs),
        "files": records,
    }
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(
        json.dumps(payload, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )


def write_csv(output: Path, records: list[dict[str, Any]]) -> None:
    output.parent.mkdir(parents=True, exist_ok=True)
    fieldnames = [
        "relative_path",
        "entry_type",
        "symlink_target",
        "size_bytes",
        "sha256",
        "extension",
        "category",
        "binary_or_undecoded",
        "detected_local_encoding",
        "line_count",
        "flags",
    ]
    with output.open("w", encoding="utf-8", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=fieldnames)
        writer.writeheader()
        for record in records:
            row = dict(record)
            row["flags"] = ";".join(record["flags"])
            writer.writerow(row)


def main() -> int:
    args = parse_args()
    root = args.root.resolve()
    if not root.is_dir():
        print(f"error: inventory root is not a directory: {root}", file=sys.stderr)
        return 2
    if args.large_file_bytes < 1:
        print("error: --large-file-bytes must be positive", file=sys.stderr)
        return 2

    output = args.output.resolve()
    csv_output = args.csv.resolve() if args.csv else None
    excluded_paths = {output}
    if csv_output:
        excluded_paths.add(csv_output)

    excluded_dirs = DEFAULT_EXCLUDED_DIRS | set(args.exclude_dir)
    files = iter_files(root, excluded_dirs, excluded_paths)
    records = [build_record(root, path, args.large_file_bytes) for path in files]
    if not records:
        print(f"error: no files found under {root}", file=sys.stderr)
        return 3

    write_json(output, root, records, excluded_dirs)
    if csv_output:
        write_csv(csv_output, records)

    flagged = sum(1 for record in records if record["flags"])
    print(
        f"Inventoried {len(records)} files ({flagged} flagged) into {output}"
        + (f" and {csv_output}" if csv_output else "")
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
