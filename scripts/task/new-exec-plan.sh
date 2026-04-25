#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

slug="${1:-}"
title="${2:-}"
[[ -n "${slug}" ]] || fail "usage: new-exec-plan.sh <slug> [title]"

validate_slug "${slug}"
load_task_env "${slug}"

[[ -z "$(find_existing_plan_for_slug "${slug}")" ]] || fail "EXEC_PLAN already exists for slug: ${slug}"

if [[ -n "${title}" ]]; then
  TASK_TITLE="${title}"
fi

filename="$(today_stamp)-${slug}.md"
EXEC_PLAN="$(plan_dir)/${filename}"

port_display="${PORT:-TBD}"

cat > "${EXEC_PLAN}" <<EOF
# EXEC_PLAN: ${TASK_TITLE}

- Task slug: \`${TASK_SLUG}\`
- Base branch: \`${BASE_BRANCH}\`
- Feature branch: \`${FEATURE_BRANCH}\`
- Worktree: \`${WORKTREE}\`
- Port: \`${port_display}\`
- Log dir: \`${LOG_DIR}\`
- Status: \`draft\`

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

write_task_env "${slug}"
printf '%s\n' "${EXEC_PLAN}"
