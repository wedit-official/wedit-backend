#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../task/common.sh
source "${SCRIPT_DIR}/../task/common.sh"

repo="$(repo_root)"
branch="$(git -C "${repo}" branch --show-current)"

[[ "${branch}" != "main" ]] || fail "main branch push는 금지됩니다."
[[ "${branch}" =~ ^codex/([a-z0-9][a-z0-9-]*)$ ]] || fail "push는 codex/<slug> 브랜치에서만 허용됩니다. PR은 scripts/task/create-pr.sh 를 사용하세요."
slug="${BASH_REMATCH[1]}"

env_file="$(task_env_path "${slug}")"
[[ -f "${env_file}" ]] || fail "task state file이 없습니다. PR은 scripts/task/create-pr.sh 를 사용하세요."

load_task_env "${slug}"
[[ -n "${EXEC_PLAN}" && -f "${EXEC_PLAN}" ]] || fail "EXEC_PLAN이 없습니다. PR은 scripts/task/create-pr.sh 를 사용하세요."
plan_is_complete "${EXEC_PLAN}" || fail "EXEC_PLAN이 비어 있습니다. PR은 scripts/task/create-pr.sh 를 사용하세요."
