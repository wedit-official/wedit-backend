#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./testlib.sh
source "${SCRIPT_DIR}/testlib.sh"

workdir="$(mktemp -d)"
trap 'rm -rf "${workdir}"' EXIT

sandbox="${workdir}/repo"
remote_repo="${workdir}/remote.git"
setup_sandbox_repo "${sandbox}"
git init --bare "${remote_repo}" >/dev/null 2>&1
git -C "${sandbox}" remote add origin "${remote_repo}"
git -C "${sandbox}" push -u origin main >/dev/null 2>&1
git -C "${sandbox}" checkout -b codex/pr-check >/dev/null 2>&1

mkdir -p "${sandbox}/docs/exec-plans/active" "${sandbox}/.codex/task-state"

cat > "${sandbox}/docs/exec-plans/active/20260425-pr-check.md" <<EOF
# EXEC_PLAN: PR Check

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [x] docs/testing/codex-harness.md

## Doc Notes
- PR 본문에는 EXEC_PLAN 경로와 검증 결과를 포함해야 한다.

## Goal
- PR 생성 스크립트를 검증한다.

## Approach
- fake gh 바이너리로 호출 인자를 수집한다.

## Step Plan
- commit 후 create-pr.sh를 실행한다.

## Done Criteria
- gh pr create가 올바른 인자로 호출된다.
EOF

cat > "${sandbox}/.codex/task-state/pr-check.env" <<EOF
TASK_SLUG='pr-check'
TASK_TITLE='PR Check'
BASE_BRANCH='main'
FEATURE_BRANCH='codex/pr-check'
EXEC_PLAN='${sandbox}/docs/exec-plans/active/20260425-pr-check.md'
WORKTREE='${workdir}/worktrees/pr-check'
PORT='18080'
LOG_DIR='${workdir}/logs/pr-check'
ISSUE_NUMBER=''
ISSUE_URL=''
ISSUE_TITLE='PR Check'
LAST_VERIFY_COMMAND='./gradlew check build --no-daemon'
LAST_VERIFY_STATUS='passed'
LAST_VERIFY_AT='2026-04-25T14:00:00+0900'
EOF

printf 'pr content\n' > "${sandbox}/feature.txt"
git -C "${sandbox}" add feature.txt
git -C "${sandbox}" commit -m "[feat] add strict workflow" >/dev/null 2>&1

gh_args_file="${workdir}/gh-args.txt"
gh_calls_file="${workdir}/gh-calls.txt"
gh_body_file="${workdir}/gh-body.txt"
stub_bin="${workdir}/bin"
mkdir -p "${stub_bin}"

cat > "${stub_bin}/gh" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

printf '%s|' "$@" >> "${GH_CALLS_FILE}"
printf '\n' >> "${GH_CALLS_FILE}"

if [[ "$1" == "issue" && "$2" == "create" ]]; then
  echo "https://github.com/example/repo/issues/123"
  exit 0
fi

body_file=""
while [[ $# -gt 0 ]]; do
  if [[ "$1" == "--body-file" ]]; then
    shift
    body_file="$1"
    break
  fi
  shift
done

if [[ -n "${body_file}" ]]; then
  cat "${body_file}" > "${GH_BODY_FILE}"
fi
EOF

chmod +x "${stub_bin}/gh"

export STRICT_REPO_ROOT="${sandbox}"
export STRICT_PLAN_DIR="${sandbox}/docs/exec-plans/active"
export STRICT_TASK_STATE_DIR="${sandbox}/.codex/task-state"
export GH_ARGS_FILE="${gh_args_file}"
export GH_CALLS_FILE="${gh_calls_file}"
export GH_BODY_FILE="${gh_body_file}"
export PATH="${stub_bin}:${PATH}"

"${TEST_ROOT}/scripts/task/create-pr.sh"

assert_file_exists "${gh_calls_file}"
assert_file_exists "${gh_body_file}"
assert_contains 'issue|create|' "${gh_calls_file}"
assert_contains 'pr|create|' "${gh_calls_file}"
assert_contains '--base' "${gh_calls_file}"
assert_contains 'main' "${gh_calls_file}"
assert_contains 'codex/pr-check' "${gh_calls_file}"
assert_contains 'Closes #123' "${gh_body_file}"
assert_contains 'https://github.com/example/repo/issues/123' "${gh_body_file}"
assert_contains 'docs/exec-plans/active/20260425-pr-check.md' "${gh_body_file}"
assert_contains 'Status: `passed`' "${gh_body_file}"
assert_contains '## Review Gate' "${gh_body_file}"
assert_contains 'Automated PR review has completed.' "${gh_body_file}"
assert_contains 'scripts/task/verify-pr-ready.sh <PR_NUMBER>' "${gh_body_file}"
