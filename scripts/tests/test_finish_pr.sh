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
submodule_repo="${workdir}/config-module"

setup_sandbox_repo "${sandbox}"
git init "${submodule_repo}" >/dev/null 2>&1
git -C "${submodule_repo}" config user.email "test@example.com"
git -C "${submodule_repo}" config user.name "Test User"
printf 'config\n' > "${submodule_repo}/config.txt"
git -C "${submodule_repo}" add config.txt
git -C "${submodule_repo}" commit -m "init config" >/dev/null 2>&1
git -C "${sandbox}" checkout develop >/dev/null 2>&1
git -C "${sandbox}" -c protocol.file.allow=always submodule add "${submodule_repo}" config >/dev/null 2>&1
git -C "${sandbox}" commit -m "add config submodule" >/dev/null 2>&1
git init --bare "${remote_repo}" >/dev/null 2>&1
git -C "${sandbox}" remote add origin "${remote_repo}"
git -C "${sandbox}" checkout develop >/dev/null 2>&1
git -C "${sandbox}" push -u origin develop >/dev/null 2>&1

git -C "${sandbox}" worktree add "${feature_worktree}" -b codex/finish-pr develop >/dev/null 2>&1
git -C "${feature_worktree}" -c protocol.file.allow=always submodule update --init config >/dev/null 2>&1
printf 'finish pr\n' > "${feature_worktree}/finish.txt"
git -C "${feature_worktree}" add finish.txt
git -C "${feature_worktree}" commit -m "[feat] finish pr" >/dev/null 2>&1
git -C "${feature_worktree}" push -u origin codex/finish-pr >/dev/null 2>&1

hooks_dir="${workdir}/hooks"
mkdir -p "${hooks_dir}"
cat > "${hooks_dir}/pre-push" <<'EOF'
#!/usr/bin/env bash
echo "pre-push hook should be bypassed for finish-pr cleanup branch deletion" >&2
exit 1
EOF
chmod +x "${hooks_dir}/pre-push"
git -C "${sandbox}" config core.hooksPath "${hooks_dir}"

stub_bin="${workdir}/bin"
mkdir -p "${stub_bin}"
gh_calls_file="${workdir}/gh-calls.txt"

cat > "${stub_bin}/gh" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

printf '%s\n' "$*" >> "${GH_CALLS_FILE}"

if [[ "$1" == "pr" && "$2" == "view" ]]; then
  reviewer_login="${GH_REVIEWER_LOGIN:-gemini-code-assist[bot]}"
  view_count=0
  if [[ -n "${GH_VIEW_COUNT_FILE:-}" ]]; then
    if [[ -f "${GH_VIEW_COUNT_FILE}" ]]; then
      view_count="$(cat "${GH_VIEW_COUNT_FILE}")"
    fi
    view_count=$((view_count + 1))
    printf '%s\n' "${view_count}" > "${GH_VIEW_COUNT_FILE}"
  fi
  head_oid="HEAD_oid"
  if [[ "${GH_HEAD_CHANGES_DURING_VERIFY:-0}" == "1" && "${view_count}" -ge 5 ]]; then
    head_oid="NEW_HEAD_oid"
  fi
  marker_head_oid="${head_oid}"
  if [[ "${GH_STALE_SUBAGENT_MARKER:-0}" == "1" ]]; then
    marker_head_oid="OLD_HEAD_oid"
  fi
  if [[ "${GH_SUBAGENT_MARKER:-1}" == "1" ]]; then
    comments_json="[{\"author\":{\"login\":\"codex\"},\"body\":\"Codex Subagent Review Gate: PASS\nHead: ${marker_head_oid}\nRound: 2/3\"}]"
  else
    comments_json='[]'
  fi
  cat <<JSON
{"number":7,"url":"https://github.com/example/repo/pull/7","state":"OPEN","id":"PR_node_7","headRefName":"codex/finish-pr","baseRefName":"develop","commits":[{"oid":"${head_oid}"}],"isDraft":false,"reviewDecision":"${GH_REVIEW_DECISION:-}","reviews":[{"author":{"login":"${reviewer_login}"},"state":"COMMENTED"}],"comments":${comments_json},"mergeStateStatus":"${GH_MERGE_STATE:-CLEAN}"}
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

head_changed_output="${workdir}/head-changed.txt"
if GH_HEAD_CHANGES_DURING_VERIFY=1 GH_VIEW_COUNT_FILE="${workdir}/head-view-count.txt" "${TEST_ROOT}/scripts/task/finish-pr.sh" 7 >"${head_changed_output}" 2>&1; then
  fail "expected finish-pr.sh to fail when PR head changes during verification"
fi
assert_contains 'head changed during verification' "${head_changed_output}"

missing_subagent_output="${workdir}/missing-subagent.txt"
if GH_SUBAGENT_MARKER=0 "${TEST_ROOT}/scripts/task/finish-pr.sh" 7 >"${missing_subagent_output}" 2>&1; then
  fail "expected finish-pr.sh to require a Codex subagent review marker"
fi
assert_contains 'missing Codex subagent review pass marker' "${missing_subagent_output}"

stale_subagent_output="${workdir}/stale-subagent.txt"
if GH_STALE_SUBAGENT_MARKER=1 "${TEST_ROOT}/scripts/task/finish-pr.sh" 7 >"${stale_subagent_output}" 2>&1; then
  fail "expected finish-pr.sh to reject a stale Codex subagent review marker"
fi
assert_contains 'missing Codex subagent review pass marker' "${stale_subagent_output}"

finish_output="$("${TEST_ROOT}/scripts/task/finish-pr.sh" 7)"
assert_output_contains 'Automated review bot activity detected' "${finish_output}"
assert_output_contains 'PR #7 merged and cleaned up' "${finish_output}"
assert_contains 'pr merge 7 --merge --match-head-commit HEAD_oid' "${gh_calls_file}"
[[ ! -d "${feature_worktree}" ]] || fail "expected feature worktree to be removed"
assert_command_fails git -C "${sandbox}" show-ref --verify --quiet refs/heads/codex/finish-pr
assert_command_fails git -C "${sandbox}" ls-remote --exit-code --heads origin codex/finish-pr
