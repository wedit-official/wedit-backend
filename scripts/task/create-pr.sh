#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

repo="$(repo_root)"
branch="$(git -C "${repo}" branch --show-current)"

[[ "${branch}" =~ ^codex/([a-z0-9][a-z0-9-]*)$ ]] || fail "PR creation requires a codex/<slug> branch: ${branch}"
slug="${BASH_REMATCH[1]}"

load_task_env "${slug}"

env_file="$(task_env_path "${slug}")"
[[ -f "${env_file}" ]] || fail "task state file is missing: ${env_file}"
[[ -n "${EXEC_PLAN}" && -f "${EXEC_PLAN}" ]] || fail "EXEC_PLAN is missing for slug: ${slug}"
plan_is_complete "${EXEC_PLAN}" || fail "EXEC_PLAN is incomplete: ${EXEC_PLAN}"

subject="$(git -C "${repo}" log -1 --pretty=%s)"
[[ "${subject}" =~ ^\[feat\]\ .+ ]] || fail "latest commit subject must match '[feat] 설명'"

remote_name="${STRICT_REMOTE_NAME:-origin}"
git -C "${repo}" push -u "${remote_name}" "${FEATURE_BRANCH}" >/dev/null

relative_plan="${EXEC_PLAN}"
if [[ "${EXEC_PLAN}" == "${repo}"/* ]]; then
  relative_plan="${EXEC_PLAN#${repo}/}"
fi

if [[ -z "${ISSUE_NUMBER}" || -z "${ISSUE_URL}" ]]; then
  issue_url="$("${SCRIPT_DIR}/create-issue.sh" "${slug}")"
  load_task_env "${slug}"
  [[ -n "${ISSUE_NUMBER}" ]] || fail "issue number is missing after issue creation: ${issue_url}"
fi

verify_status="${LAST_VERIFY_STATUS:-unknown}"
verify_command="${LAST_VERIFY_COMMAND:-./gradlew check build --no-daemon}"
verify_at="${LAST_VERIFY_AT:-unknown}"

body_file="$(mktemp)"
trap 'rm -f "${body_file}"' EXIT

cat > "${body_file}" <<EOF
Closes #${ISSUE_NUMBER}

## Issue
- #${ISSUE_NUMBER}
- ${ISSUE_URL}

## EXEC_PLAN
- \`${relative_plan}\`

## Verification
- Status: \`${verify_status}\`
- Command: \`${verify_command}\`
- At: \`${verify_at}\`

## Review Gate
- [ ] Automated PR review has completed.
- [ ] Every actionable PR review comment/thread is addressed and resolved.
- [ ] \`scripts/task/verify-pr-ready.sh <PR_NUMBER>\` passes before merge.
- [ ] \`scripts/task/finish-pr.sh <PR_NUMBER>\` completes merge, develop sync, worktree removal, and branch cleanup.
EOF

gh pr create \
  --base "${BASE_BRANCH}" \
  --head "${FEATURE_BRANCH}" \
  --title "${subject}" \
  --body-file "${body_file}"
