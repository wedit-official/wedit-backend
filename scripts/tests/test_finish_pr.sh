#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./testlib.sh
source "${SCRIPT_DIR}/testlib.sh"

workdir="$(mktemp -d)"
trap 'rm -rf "${workdir}"' EXIT

sandbox="${workdir}/repo"
remote_repo="${workdir}/remote.git"
feature_worktree="${workdir}/feature-worktree"

setup_sandbox_repo "${sandbox}"
git init --bare "${remote_repo}" >/dev/null 2>&1
git -C "${sandbox}" remote add origin "${remote_repo}"
git -C "${sandbox}" checkout develop >/dev/null 2>&1
git -C "${sandbox}" push -u origin develop >/dev/null 2>&1

git -C "${sandbox}" worktree add "${feature_worktree}" -b codex/finish-pr develop >/dev/null 2>&1
printf 'finish pr\n' > "${feature_worktree}/finish.txt"
git -C "${feature_worktree}" add finish.txt
git -C "${feature_worktree}" commit -m "[feat] finish pr" >/dev/null 2>&1
git -C "${feature_worktree}" push -u origin codex/finish-pr >/dev/null 2>&1

stub_bin="${workdir}/bin"
mkdir -p "${stub_bin}"
gh_calls_file="${workdir}/gh-calls.txt"

cat > "${stub_bin}/gh" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

printf '%s\n' "$*" >> "${GH_CALLS_FILE}"

if [[ "$1" == "pr" && "$2" == "view" ]]; then
  reviewer_login="${GH_REVIEWER_LOGIN:-gemini-code-assist[bot]}"
  cat <<JSON
{"number":7,"url":"https://github.com/example/repo/pull/7","state":"OPEN","id":"PR_node_7","headRefName":"codex/finish-pr","baseRefName":"develop","commits":[{"oid":"HEAD_oid"}],"isDraft":false,"reviewDecision":"${GH_REVIEW_DECISION:-}","reviews":[{"author":{"login":"${reviewer_login}"},"state":"COMMENTED"}],"comments":[],"mergeStateStatus":"${GH_MERGE_STATE:-CLEAN}"}
JSON
  exit 0
fi

if [[ "$1" == "pr" && "$2" == "checks" ]]; then
  exit "${GH_CHECKS_EXIT:-0}"
fi

if [[ "$1" == "api" && "$2" == "graphql" ]]; then
  if [[ "${GH_UNRESOLVED_THREADS:-0}" == "1" ]]; then
    cat <<JSON
{"data":{"node":{"reviewThreads":{"pageInfo":{"hasNextPage":false},"nodes":[{"isResolved":false,"path":"src/main/java/App.java","line":12,"comments":{"nodes":[{"author":{"login":"gemini-code-assist[bot]"},"url":"https://github.com/example/repo/pull/7#discussion_r1"}]}}]}}}}
JSON
  else
    cat <<JSON
{"data":{"node":{"reviewThreads":{"pageInfo":{"hasNextPage":false},"nodes":[]}}}}
JSON
  fi
  exit 0
fi

if [[ "$1" == "pr" && "$2" == "merge" ]]; then
  git -C "${GH_DEVELOP_WORKTREE}" merge --no-ff codex/finish-pr -m "Merge PR #7" >/dev/null 2>&1
  exit 0
fi

echo "unexpected gh call: $*" >&2
exit 2
EOF

chmod +x "${stub_bin}/gh"

export STRICT_REPO_ROOT="${sandbox}"
export GH_CALLS_FILE="${gh_calls_file}"
export GH_DEVELOP_WORKTREE="${sandbox}"
export PATH="${stub_bin}:${PATH}"

missing_gemini_output="${workdir}/missing-gemini.txt"
if STRICT_REVIEW_BOT_TIMEOUT_SECONDS=0 GH_REVIEWER_LOGIN=review-bot "${TEST_ROOT}/scripts/task/finish-pr.sh" 7 >"${missing_gemini_output}" 2>&1; then
  fail "expected finish-pr.sh to wait for Gemini bot activity before merging"
fi
assert_contains 'timed out waiting for automated review bot activity' "${missing_gemini_output}"

unresolved_output="${workdir}/unresolved.txt"
if GH_UNRESOLVED_THREADS=1 "${TEST_ROOT}/scripts/task/finish-pr.sh" 7 >"${unresolved_output}" 2>&1; then
  fail "expected finish-pr.sh to fail on unresolved review threads"
fi
assert_contains 'unresolved PR review threads' "${unresolved_output}"

finish_output="$("${TEST_ROOT}/scripts/task/finish-pr.sh" 7)"
assert_output_contains 'Automated review bot activity detected' "${finish_output}"
assert_output_contains 'PR #7 merged and cleaned up' "${finish_output}"
assert_contains 'pr merge 7 --merge --match-head-commit HEAD_oid' "${gh_calls_file}"
[[ ! -d "${feature_worktree}" ]] || fail "expected feature worktree to be removed"
assert_command_fails git -C "${sandbox}" show-ref --verify --quiet refs/heads/codex/finish-pr
