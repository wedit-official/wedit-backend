#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

slug="${1:-}"
title="${2:-}"
[[ -n "${slug}" ]] || {
  echo "Error: usage: init-task.sh <slug> [title]" >&2
  exit 1
}

"${SCRIPT_DIR}/new-worktree.sh" "${slug}"
"${SCRIPT_DIR}/allocate-port.sh" "${slug}"
"${SCRIPT_DIR}/create-log-dir.sh" "${slug}"
"${SCRIPT_DIR}/new-exec-plan.sh" "${slug}" "${title}"
