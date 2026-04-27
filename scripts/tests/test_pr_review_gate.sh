#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./testlib.sh
source "${SCRIPT_DIR}/testlib.sh"

workdir="$(mktemp -d)"
trap 'rm -rf "${workdir}"' EXIT

sandbox="${workdir}/repo"
setup_sandbox_repo "${sandbox}"
git -C "${sandbox}" checkout -b codex/review-gate >/dev/null 2>&1

stub_bin="${workdir}/bin"
mkdir -p "${stub_bin}"

gh_calls_file="${workdir}/gh-calls.txt"

cat > "${stub_bin}/gh" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

printf '%s\n' "$*" >> "${GH_CALLS_FILE}"

if [[ "$1" == "pr" && "$2" == "view" ]]; then
  if [[ "${GH_REVIEW_ACTIVITY_COUNT:-1}" == "0" ]]; then
    reviews_json='[]'
    comments_json='[]'
  else
    reviews_json='[{"author":{"login":"review-bot"},"state":"COMMENTED"}]'
    comments_json='[]'
  fi

  cat <<JSON
{"number":7,"url":"https://github.com/example/repo/pull/7","state":"${GH_PR_STATE:-OPEN}","isDraft":${GH_PR_DRAFT:-false},"reviewDecision":"${GH_REVIEW_DECISION:-}","reviews":${reviews_json},"comments":${comments_json},"id":"PR_node_7","mergeStateStatus":"${GH_MERGE_STATE:-CLEAN}"}
JSON
  exit 0
fi

if [[ "$1" == "pr" && "$2" == "checks" ]]; then
  exit "${GH_CHECKS_EXIT:-0}"
fi

if [[ "$1" == "api" && "$2" == "graphql" ]]; then
  if [[ "${GH_UNRESOLVED_THREADS:-0}" == "1" ]]; then
    cat <<JSON
{"data":{"node":{"reviewThreads":{"pageInfo":{"hasNextPage":${GH_THREADS_HAS_NEXT:-false}},"nodes":[{"isResolved":false,"path":"src/main/java/App.java","line":12,"comments":{"nodes":[{"author":{"login":"reviewer"},"url":"https://github.com/example/repo/pull/7#discussion_r1"}]}}]}}}}
JSON
  else
    cat <<JSON
{"data":{"node":{"reviewThreads":{"pageInfo":{"hasNextPage":${GH_THREADS_HAS_NEXT:-false}},"nodes":[]}}}}
JSON
  fi
  exit 0
fi

echo "unexpected gh call: $*" >&2
exit 2
EOF

chmod +x "${stub_bin}/gh"

export STRICT_REPO_ROOT="${sandbox}"
export GH_CALLS_FILE="${gh_calls_file}"
export PATH="${stub_bin}:${PATH}"

ready_output="$("${TEST_ROOT}/scripts/task/verify-pr-ready.sh" 7)"
assert_output_contains 'PR #7 is ready to merge' "${ready_output}"
assert_contains 'pr checks 7 --watch --fail-fast' "${gh_calls_file}"

unresolved_output="${workdir}/unresolved.txt"
if GH_UNRESOLVED_THREADS=1 "${TEST_ROOT}/scripts/task/verify-pr-ready.sh" 7 >"${unresolved_output}" 2>&1; then
  fail "expected unresolved review threads to fail the gate"
fi
assert_contains 'unresolved PR review threads' "${unresolved_output}"

changes_requested_output="${workdir}/changes-requested.txt"
if GH_REVIEW_DECISION=CHANGES_REQUESTED "${TEST_ROOT}/scripts/task/verify-pr-ready.sh" 7 >"${changes_requested_output}" 2>&1; then
  fail "expected requested changes to fail the gate"
fi
assert_contains 'requested changes' "${changes_requested_output}"

no_activity_output="${workdir}/no-activity.txt"
if GH_REVIEW_ACTIVITY_COUNT=0 "${TEST_ROOT}/scripts/task/verify-pr-ready.sh" 7 >"${no_activity_output}" 2>&1; then
  fail "expected missing automated review activity to fail the gate"
fi
assert_contains 'no review/comment activity yet' "${no_activity_output}"
