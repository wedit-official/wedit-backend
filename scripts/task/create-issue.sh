#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

repo="$(repo_root)"
slug="${1:-}"
title="${2:-}"

if [[ -z "${slug}" ]]; then
  branch="$(git -C "${repo}" branch --show-current)"
  [[ "${branch}" =~ ^codex/([a-z0-9][a-z0-9-]*)$ ]] || fail "issue creation requires a codex/<slug> branch or an explicit slug"
  slug="${BASH_REMATCH[1]}"
fi

validate_slug "${slug}"
load_task_env "${slug}"

env_file="$(task_env_path "${slug}")"
[[ -f "${env_file}" ]] || fail "task state file is missing: ${env_file}"
[[ -n "${EXEC_PLAN}" && -f "${EXEC_PLAN}" ]] || fail "EXEC_PLAN is missing for slug: ${slug}"
plan_is_complete "${EXEC_PLAN}" || fail "EXEC_PLAN is incomplete: ${EXEC_PLAN}"

if [[ -n "${ISSUE_NUMBER}" && -n "${ISSUE_URL}" ]]; then
  printf '%s\n' "${ISSUE_URL}"
  exit 0
fi

if [[ -n "${title}" ]]; then
  ISSUE_TITLE="${title}"
elif [[ -z "${ISSUE_TITLE:-}" || "${ISSUE_TITLE}" == "${TASK_SLUG}" ]]; then
  ISSUE_TITLE="${TASK_TITLE}"
fi

relative_plan="${EXEC_PLAN}"
if [[ "${EXEC_PLAN}" == "${repo}"/* ]]; then
  relative_plan="${EXEC_PLAN#${repo}/}"
fi

goal_summary="$(plan_first_nonempty_bullet "${EXEC_PLAN}" "Goal")"
approach_summary="$(plan_first_nonempty_bullet "${EXEC_PLAN}" "Approach")"

body_file="$(mktemp)"
trap 'rm -f "${body_file}"' EXIT

cat > "${body_file}" <<EOF
## EXEC_PLAN
- \`${relative_plan}\`

## Goal
- ${goal_summary:-See EXEC_PLAN}

## Approach
- ${approach_summary:-See EXEC_PLAN}
EOF

issue_url="$(gh issue create --title "${ISSUE_TITLE}" --body-file "${body_file}")"
ISSUE_URL="${issue_url}"
ISSUE_NUMBER="${issue_url##*/}"
write_task_env "${slug}"

printf '%s\n' "${ISSUE_URL}"
