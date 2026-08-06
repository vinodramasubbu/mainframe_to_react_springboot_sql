from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch


SCRIPT = Path(__file__).parents[1] / "scripts" / "inventory_sources.py"
SPEC = importlib.util.spec_from_file_location("inventory_sources", SCRIPT)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError(f"Unable to load {SCRIPT}")
inventory_sources = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(inventory_sources)


class InventorySourcesTests(unittest.TestCase):
    def test_external_symlink_preserves_link_identity_without_dereferencing(self) -> None:
        with tempfile.TemporaryDirectory() as root_name:
            root = Path(root_name).resolve()
            link = root / "linked.cbl"
            link.write_text("link-placeholder", encoding="utf-8")
            external_target = "C:\\external-extraction\\target.cbl"

            files = inventory_sources.iter_files(root, set(), set())
            self.assertEqual([link], files)

            with (
                patch.object(inventory_sources, "is_link_like", return_value=True),
                patch.object(inventory_sources.os, "readlink", return_value=external_target),
            ):
                record = inventory_sources.build_record(root, files[0], 1024)

            self.assertEqual("linked.cbl", record["relative_path"])
            self.assertEqual("symbolic-link", record["entry_type"])
            self.assertEqual(external_target, record["symlink_target"])
            self.assertEqual("symbolic-link", record["category"])
            self.assertEqual(["symbolic-link"], record["flags"])

    def test_link_like_directory_is_recorded_without_traversal(self) -> None:
        with tempfile.TemporaryDirectory() as root_name:
            root = Path(root_name).resolve()
            link_directory = root / "dir-link"
            link_directory.mkdir()
            (link_directory / "outside.cbl").write_text("SHOULD NOT BE VISITED", encoding="utf-8")

            with patch.object(
                inventory_sources,
                "is_link_like",
                side_effect=lambda path: path == link_directory,
            ):
                files = inventory_sources.iter_files(root, set(), set())

            self.assertEqual([link_directory], files)

    def test_default_inventory_includes_bin_and_obj_directories(self) -> None:
        with tempfile.TemporaryDirectory() as root_name:
            root = Path(root_name).resolve()
            bin_file = root / "bin" / "run.rexx"
            obj_file = root / "obj" / "module.obj"
            bin_file.parent.mkdir()
            obj_file.parent.mkdir()
            bin_file.write_text("say 'run'\n", encoding="utf-8")
            obj_file.write_bytes(b"object-evidence")

            files = inventory_sources.iter_files(
                root,
                inventory_sources.DEFAULT_EXCLUDED_DIRS,
                set(),
            )

            self.assertEqual([bin_file, obj_file], files)


if __name__ == "__main__":
    unittest.main()