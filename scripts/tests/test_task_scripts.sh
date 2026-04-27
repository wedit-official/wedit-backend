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
export STRICT_PLAN_DIR="${sandbox}/docs/exec-plans/active"
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
assert_dir_exists "${LOG_DIR}"
assert_contains '## Required Reads' "${EXEC_PLAN}"
assert_contains '## Related Docs' "${EXEC_PLAN}"
assert_contains '## Related Feature IDs' "${EXEC_PLAN}"
assert_contains '## Doc Notes' "${EXEC_PLAN}"
assert_contains '- [ ] AGENTS.md' "${EXEC_PLAN}"
assert_contains '- [ ] ARCHITECTURE.md' "${EXEC_PLAN}"
assert_contains '- [ ] docs/index.md' "${EXEC_PLAN}"
assert_contains '- [ ] docs/...' "${EXEC_PLAN}"
assert_contains '- [ ] <feature-id>' "${EXEC_PLAN}"
assert_contains '## Goal' "${EXEC_PLAN}"
assert_contains '## Approach' "${EXEC_PLAN}"
assert_contains '## Step Plan' "${EXEC_PLAN}"
assert_contains '## Done Criteria' "${EXEC_PLAN}"
assert_contains $'strict-workflow\t18080' "${STRICT_PORT_REGISTRY}"

assert_command_fails "${TEST_ROOT}/scripts/task/new-exec-plan.sh" strict-workflow "Duplicate"
assert_command_fails "${TEST_ROOT}/scripts/task/new-worktree.sh" strict-workflow
assert_command_fails "${TEST_ROOT}/scripts/task/new-exec-plan.sh" bad_slug "Bad Slug"
