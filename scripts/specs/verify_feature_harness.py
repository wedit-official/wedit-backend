#!/usr/bin/env python3

from __future__ import annotations

import argparse
import csv
from pathlib import Path

from normalize_feature_spec import (
    DEFAULT_OUTPUT,
    DEFAULT_RAW_SPEC,
    OUTPUT_FIELDS,
    build_feature_rows,
    load_raw_rows,
)


REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_MATRIX = REPO_ROOT / "docs/specs/2026-wedit-backend-coverage-matrix.md"

ALLOWED_STATUSES = {
    "implemented",
    "planned",
    "gap",
    "out_of_scope_for_backend",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Validate the normalized backend feature spec and coverage matrix."
    )
    parser.add_argument("--raw", type=Path, default=DEFAULT_RAW_SPEC, help="Primary raw CSV export.")
    parser.add_argument("--generated", type=Path, default=DEFAULT_OUTPUT, help="Generated normalized CSV.")
    parser.add_argument("--matrix", type=Path, default=DEFAULT_MATRIX, help="Markdown coverage matrix.")
    parser.add_argument("--repo-root", type=Path, default=REPO_ROOT, help="Repository root for path checks.")
    return parser.parse_args()


def load_generated_rows(path: Path) -> list[dict[str, str]]:
    with path.open(newline="", encoding="utf-8") as handle:
        reader = csv.DictReader(handle)
        return [
            {field: row.get(field, "").strip() for field in OUTPUT_FIELDS}
            for row in reader
        ]


def parse_matrix_rows(matrix_path: Path) -> list[dict[str, str]]:
    lines = matrix_path.read_text(encoding="utf-8").splitlines()
    header: list[str] | None = None
    rows: list[dict[str, str]] = []
    table_started = False

    for line in lines:
        stripped = line.strip()
        if not stripped.startswith("|"):
            if table_started and rows:
                break
            continue

        cells = [cell.strip() for cell in stripped.strip("|").split("|")]
        if not table_started and cells and cells[0] == "feature_id":
            header = cells
            table_started = True
            continue

        if table_started and cells and all(cell.startswith("-") or cell == "" for cell in cells):
            continue

        if table_started and header is not None:
            if len(cells) != len(header):
                raise ValueError(f"Matrix row has {len(cells)} cells, expected {len(header)}: {line}")
            rows.append(dict(zip(header, cells)))

    if not rows:
        raise ValueError(f"Could not find a coverage matrix table in {matrix_path}")

    return rows


def normalize_path_tokens(value: str) -> list[str]:
    if value.strip() in {"", "-"}:
        return []

    tokens = []
    for token in value.split(";"):
        token = token.strip().strip("`")
        if token and token != "-":
            tokens.append(token)
    return tokens


def ensure_paths_exist(repo_root: Path, paths: list[str], column_name: str, feature_id: str) -> None:
    for path in paths:
        if path.startswith("http://") or path.startswith("https://"):
            continue
        if not (repo_root / path).exists():
            raise ValueError(
                f"{column_name} path does not exist for {feature_id}: {path}"
            )


def compare_generated(expected_rows: list[dict[str, str]], actual_rows: list[dict[str, str]]) -> None:
    if expected_rows != actual_rows:
        raise ValueError("Generated CSV is stale. Re-run normalize_feature_spec.py.")


def validate_matrix(
    generated_rows: list[dict[str, str]],
    matrix_rows: list[dict[str, str]],
    repo_root: Path,
) -> None:
    generated_by_id = {row["feature_id"]: row for row in generated_rows}
    seen_ids: set[str] = set()

    if len(matrix_rows) != len(generated_rows):
        raise ValueError(
            f"Coverage matrix row count mismatch: expected {len(generated_rows)}, found {len(matrix_rows)}."
        )

    for row in matrix_rows:
        feature_id = row["feature_id"].strip().strip("`")
        if feature_id in seen_ids:
            raise ValueError(f"Duplicate feature_id in coverage matrix: {feature_id}")
        seen_ids.add(feature_id)

        generated = generated_by_id.get(feature_id)
        if generated is None:
            raise ValueError(f"Coverage matrix references unknown feature_id: {feature_id}")

        status = row["status"].strip().strip("`")
        if status not in ALLOWED_STATUSES:
            raise ValueError(f"Unsupported status for {feature_id}: {status}")

        if generated["backend_scope"] == "out_of_scope_for_backend" and status != "out_of_scope_for_backend":
            raise ValueError(
                f"{feature_id} must stay out_of_scope_for_backend because the normalized spec marks it as frontend-only."
            )

        if generated["backend_scope"] != "out_of_scope_for_backend" and status == "out_of_scope_for_backend":
            raise ValueError(
                f"{feature_id} is backend-scoped in the normalized spec but marked out_of_scope_for_backend in the matrix."
            )

        implementation_paths = normalize_path_tokens(row["implementation_evidence"])
        test_paths = normalize_path_tokens(row["test_evidence"])

        ensure_paths_exist(repo_root, implementation_paths, "implementation_evidence", feature_id)
        ensure_paths_exist(repo_root, test_paths, "test_evidence", feature_id)

        if status == "implemented":
            if not implementation_paths:
                raise ValueError(f"{feature_id} is implemented but has no implementation evidence.")
            if not test_paths:
                raise ValueError(f"{feature_id} is implemented but has no test evidence.")

        next_step = row["next_step"].strip()
        if len(next_step) < 5 or next_step.lower() in {"", "-", "tbd", "todo"}:
            raise ValueError(f"{feature_id} must include a meaningful next_step entry.")

    missing_ids = sorted(set(generated_by_id) - seen_ids)
    if missing_ids:
        raise ValueError(f"Coverage matrix is missing feature_ids: {', '.join(missing_ids)}")


def main() -> int:
    args = parse_args()
    expected_rows = build_feature_rows(load_raw_rows(args.raw), args.raw)
    actual_rows = load_generated_rows(args.generated)
    compare_generated(expected_rows, actual_rows)
    matrix_rows = parse_matrix_rows(args.matrix)
    validate_matrix(actual_rows, matrix_rows, args.repo_root)
    print("Feature harness verification passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
