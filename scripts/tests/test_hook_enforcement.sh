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
export STRICT_VERIFY_COMMAND=true

printf 'class MainOnly {}\n' > "${sandbox}/src/main/java/MainOnly.java"
git -C "${sandbox}" add src/main/java/MainOnly.java
assert_command_fails "${TEST_ROOT}/scripts/hooks/pre-commit.sh"
git -C "${sandbox}" reset HEAD src/main/java/MainOnly.java >/dev/null 2>&1

git -C "${sandbox}" checkout -b codex/hook-check >/dev/null 2>&1

cat > "${sandbox}/.codex/task-state/hook-check.env" <<EOF
TASK_SLUG='hook-check'
TASK_TITLE='Hook Check'
BASE_BRANCH='main'
FEATURE_BRANCH='codex/hook-check'
EXEC_PLAN='${sandbox}/docs/exec-plans/active/20260425-hook-check.md'
WORKTREE='${workdir}/worktrees/hook-check'
PORT='18080'
LOG_DIR='${workdir}/logs/hook-check'
LAST_VERIFY_COMMAND=''
LAST_VERIFY_STATUS=''
LAST_VERIFY_AT=''
EOF

cat > "${sandbox}/docs/exec-plans/active/20260425-hook-check.md" <<EOF
# EXEC_PLAN: Hook Check

## Required Reads
- [ ] AGENTS.md
- [ ] ARCHITECTURE.md
- [ ] docs/index.md

## Related Docs
- [ ] docs/...

## Doc Notes
TBD

## Goal
TBD

## Approach
TBD

## Step Plan
TBD

## Done Criteria
TBD
EOF

printf 'class NeedsTest {}\n' > "${sandbox}/src/main/java/NeedsTest.java"
git -C "${sandbox}" add src/main/java/NeedsTest.java
assert_command_fails "${TEST_ROOT}/scripts/hooks/pre-commit.sh"

cat > "${sandbox}/docs/exec-plans/active/20260425-hook-check.md" <<EOF
# EXEC_PLAN: Hook Check

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [ ] docs/index.md

## Related Docs
- [ ] docs/...

## Doc Notes
TBD

## Goal
- Hook 규칙을 검증한다.

## Approach
- sandbox 저장소에서 staged 변경을 만든다.

## Step Plan
- pre-commit을 통과하는 케이스를 만든다.

## Done Criteria
- pre-commit이 성공한다.
EOF

assert_command_fails "${TEST_ROOT}/scripts/hooks/pre-commit.sh"

cat > "${sandbox}/docs/exec-plans/active/20260425-hook-check.md" <<EOF
# EXEC_PLAN: Hook Check

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [ ] docs/...

## Doc Notes
TBD

## Goal
- Hook 규칙을 검증한다.

## Approach
- sandbox 저장소에서 staged 변경을 만든다.

## Step Plan
- pre-commit을 통과하는 케이스를 만든다.

## Done Criteria
- pre-commit이 성공한다.
EOF

assert_command_fails "${TEST_ROOT}/scripts/hooks/pre-commit.sh"

cat > "${sandbox}/docs/exec-plans/active/20260425-hook-check.md" <<EOF
# EXEC_PLAN: Hook Check

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [x] docs/testing/codex-harness.md

## Doc Notes
TBD

## Goal
- Hook 규칙을 검증한다.

## Approach
- sandbox 저장소에서 staged 변경을 만든다.

## Step Plan
- pre-commit을 통과하는 케이스를 만든다.

## Done Criteria
- pre-commit이 성공한다.
EOF

assert_command_fails "${TEST_ROOT}/scripts/hooks/pre-commit.sh"

cat > "${sandbox}/docs/exec-plans/active/20260425-hook-check.md" <<EOF
# EXEC_PLAN: Hook Check

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [x] docs/testing/codex-harness.md

## Related Feature IDs
- [x] n/a-harness

## Doc Notes
- H2 기반 test profile과 표준 검증 명령을 유지해야 한다.

## Goal
- Hook 규칙을 검증한다.

## Approach
- sandbox 저장소에서 staged 변경을 만든다.

## Step Plan
- pre-commit을 통과하는 케이스를 만든다.

## Done Criteria
- pre-commit이 성공한다.
EOF

printf 'class NeedsTestSpec {}\n' > "${sandbox}/src/test/java/NeedsTestSpec.java"
git -C "${sandbox}" add src/test/java/NeedsTestSpec.java
"${TEST_ROOT}/scripts/hooks/pre-commit.sh"

message_file="${workdir}/commit-message.txt"
printf 'bad message\n' > "${message_file}"
assert_command_fails "${TEST_ROOT}/scripts/hooks/commit-msg.sh" "${message_file}"

printf '[feat] add hook checks\n' > "${message_file}"
"${TEST_ROOT}/scripts/hooks/commit-msg.sh" "${message_file}"

git -C "${sandbox}" checkout main >/dev/null 2>&1
assert_command_fails "${TEST_ROOT}/scripts/hooks/pre-push.sh" origin local </dev/null
