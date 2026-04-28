#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./testlib.sh
source "${SCRIPT_DIR}/testlib.sh"

workdir="$(mktemp -d)"
trap 'rm -rf "${workdir}"' EXIT

sandbox="${workdir}/repo"
setup_sandbox_repo "${sandbox}"

export STRICT_REPO_ROOT="${sandbox}"
export STRICT_TASK_STATE_DIR="${sandbox}/.codex/task-state"
export STRICT_WORKTREE_BASE_DIR="${workdir}/worktrees"
export STRICT_LOG_BASE_DIR="${workdir}/logs"
export STRICT_PORT_REGISTRY="${sandbox}/.codex/task-state/ports.tsv"

"${TEST_ROOT}/scripts/task/init-task.sh" strict-workflow "Strict Workflow"

env_file="${sandbox}/.codex/task-state/strict-workflow.env"
assert_file_exists "${env_file}"
# shellcheck disable=SC1090
source "${env_file}"

assert_file_exists "${EXEC_PLAN}"
assert_dir_exists "${WORKTREE}"
shared_state_dir="$(
  cd "${WORKTREE}"
  env -u STRICT_REPO_ROOT -u STRICT_TASK_STATE_DIR bash -c 'source "$1"; task_state_dir' _ "${TEST_ROOT}/scripts/task/common.sh"
)"
expected_state_dir="$(cd "${sandbox}/.codex/task-state" && pwd -P)"
[[ "${shared_state_dir}" == "${expected_state_dir}" ]] ||
  fail "expected linked worktrees to share base task state: ${shared_state_dir}"
case "${EXEC_PLAN}" in
  "${WORKTREE}/docs/exec-plans/active/"*) ;;
  *) fail "expected EXEC_PLAN to be created inside the feature worktree: ${EXEC_PLAN}" ;;
esac
[[ ! -f "${sandbox}/docs/exec-plans/active/$(basename "${EXEC_PLAN}")" ]] ||
  fail "EXEC_PLAN should not be left in the base develop worktree"
assert_dir_exists "${LOG_DIR}"
assert_contains '## Required Reads' "${EXEC_PLAN}"
assert_contains '## Related Docs' "${EXEC_PLAN}"
assert_contains '## Related Feature IDs' "${EXEC_PLAN}"
assert_contains '## Doc Notes' "${EXEC_PLAN}"
assert_contains '- [ ] AGENTS.md' "${EXEC_PLAN}"
assert_contains '- [ ] ARCHITECTURE.md' "${EXEC_PLAN}"
assert_contains '- [ ] docs/index.md' "${EXEC_PLAN}"
assert_contains '- [ ] docs/...' "${EXEC_PLAN}"
assert_contains '- [ ] feature-id-or-n/a-harness' "${EXEC_PLAN}"
assert_contains '## Goal' "${EXEC_PLAN}"
assert_contains '## Approach' "${EXEC_PLAN}"
assert_contains '## Step Plan' "${EXEC_PLAN}"
assert_contains '## Done Criteria' "${EXEC_PLAN}"
assert_contains $'strict-workflow\t18080' "${STRICT_PORT_REGISTRY}"
if grep -Fq -- "${workdir}" "${EXEC_PLAN}"; then
  fail "EXEC_PLAN should not expose local absolute sandbox paths"
fi

assert_command_fails env STRICT_REPO_ROOT="${WORKTREE}" "${TEST_ROOT}/scripts/task/new-exec-plan.sh" strict-workflow "Duplicate"
assert_command_fails "${TEST_ROOT}/scripts/task/new-worktree.sh" strict-workflow
assert_command_fails "${TEST_ROOT}/scripts/task/new-exec-plan.sh" bad_slug "Bad Slug"
