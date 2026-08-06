#!/usr/bin/env python3
"""Deny agent tool calls that may mutate immutable legacy source evidence."""

from __future__ import annotations

import json
import re
import sys
from typing import Any

READ_ONLY_TOOLS = {
    "file_search",
    "grep_search",
    "list_dir",
    "read_file",
    "semantic_search",
    "view_image",
    "vscode_listCodeUsages",
}
MUTATING_TOOLS = {
    "apply_patch",
    "create_file",
    "run_in_terminal",
    "send_to_terminal",
    "vscode_renameSymbol",
}
PATH_KEYS = {"filePath", "path", "old_path", "new_path", "uri"}
PROTECTED_DIRECTORY = "legacy-source"


def is_protected_path(value: str) -> bool:
    parts = value.replace("\\", "/").lower().strip("/").split("/")
    return PROTECTED_DIRECTORY in parts


def targets_protected_path(value: Any) -> bool:
    if isinstance(value, list):
        return any(targets_protected_path(item) for item in value)
    if not isinstance(value, dict):
        return False

    for key, item in value.items():
        if key in PATH_KEYS and isinstance(item, str) and is_protected_path(item):
            return True
        if key == "input" and isinstance(item, str):
            headers = re.findall(
                r"^\*\*\* (?:Add|Update|Delete) File: (.+)$", item, re.MULTILINE
            )
            if any(is_protected_path(header.strip()) for header in headers):
                return True
        if isinstance(item, (dict, list)) and targets_protected_path(item):
            return True

    return False


def main() -> int:
    try:
        payload = json.load(sys.stdin)
    except (json.JSONDecodeError, OSError):
        return 0

    tool_name = str(
        payload.get("tool_name")
        or payload.get("toolName")
        or payload.get("tool", {}).get("name", "")
    ).split(".")[-1]

    if (
        targets_protected_path(payload)
        and tool_name in MUTATING_TOOLS
        and tool_name not in READ_ONLY_TOOLS
    ):
        response = {
            "hookSpecificOutput": {
                "hookEventName": "PreToolUse",
                "permissionDecision": "deny",
                "permissionDecisionReason": (
                    "Legacy source is immutable forensic evidence; write annotations "
                    "or normalized artifacts under modernization instead"
                ),
            }
        }
        json.dump(response, sys.stdout)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
