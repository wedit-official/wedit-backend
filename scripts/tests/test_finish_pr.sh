#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./testlib.sh
source "${SCRIPT_DIR}/testlib.sh"

workdir="$(mktemp -d)"
trap 'rm -rf "${workdir}"' EXIT

sandbox="${workdir}/repo"
remote_repo="${workdir}/remote.git"
feature_worktree="${workdir}/worktrees/finish-cleanup"
setup_sandbox_repo "${sandbox}"
git init --bare "${remote_repo}" >/dev/null 2>&1
git -C "${sandbox}" remote add origin "${remote_repo}"
git -C "${sandbox}" checkout develop >/dev/null 2>&1
git -C "${sandbox}" push -u origin main develop >/dev/null 2>&1

git -C "${sandbox}" checkout -b codex/finish-cleanup develop >/dev/null 2>&1
printf 'finished\n' > "${sandbox}/finished.txt"
git -C "${sandbox}" add finished.txt
git -C "${sandbox}" commit -m "[feat] finish cleanup" >/dev/null 2>&1
git -C "${sandbox}" push -u origin codex/finish-cleanup >/dev/null 2>&1
git -C "${sandbox}" checkout develop >/dev/null 2>&1
git -C "${sandbox}" worktree add "${feature_worktree}" codex/finish-cleanup >/dev/null 2>&1

stub_bin="${workdir}/bin"
mkdir -p "${stub_bin}"
gh_calls_file="${workdir}/gh-calls.txt"
threads_resolved_file="${workdir}/threads-resolved"

cat > "${stub_bin}/gh" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

printf '%s\n' "$*" >> "${GH_CALLS_FILE}"

if [[ "$1" == "pr" && "$2" == "view" ]]; then
  cat <<JSON
{"number":7,"url":"https://github.com/example/repo/pull/7","state":"OPEN","id":"PR_node_7","headRefName":"codex/finish-cleanup","baseRefName":"develop","isDraft":false,"reviewDecision":"","reviews":[{"author":{"login":"review-bot"},"state":"COMMENTED"}],"comments":[],"mergeStateStatus":"CLEAN"}
JSON
  exit 0
fi

if [[ "$1" == "api" && "$2" == "graphql" ]]; then
  if [[ "$*" == *"resolveReviewThread"* ]]; then
    touch "${GH_THREADS_RESOLVED_FILE}"
    cat <<'JSON'
{"data":{"resolveReviewThread":{"thread":{"id":"THREAD_1","isResolved":true}}}}
JSON
    exit 0
  fi

  if [[ -f "${GH_THREADS_RESOLVED_FILE}" ]]; then
    cat <<'JSON'
{"data":{"node":{"reviewThreads":{"pageInfo":{"hasNextPage":false},"nodes":[]}}}}
JSON
  else
    cat <<'JSON'
{"data":{"node":{"reviewThreads":{"pageInfo":{"hasNextPage":false},"nodes":[{"id":"THREAD_1","isResolved":false,"path":"scripts/task/finish-pr.sh","line":12,"comments":{"nodes":[{"author":{"login":"reviewer"},"url":"https://github.com/example/repo/pull/7#discussion_r1"}]}}]}}}}
JSON
  fi
  exit 0
fi

if [[ "$1" == "pr" && "$2" == "checks" ]]; then
  exit 0
fi

if [[ "$1" == "pr" && "$2" == "merge" ]]; then
  git -C "${SANDBOX_REPO}" checkout develop >/dev/null 2>&1
  git -C "${SANDBOX_REPO}" merge --no-ff codex/finish-cleanup -m "Merge pull request #7 from example/codex/finish-cleanup" >/dev/null 2>&1
  git -C "${SANDBOX_REPO}" push origin develop >/dev/null 2>&1
  exit 0
fi

echo "unexpected gh call: $*" >&2
exit 2
EOF

chmod +x "${stub_bin}/gh"

export STRICT_REPO_ROOT="${feature_worktree}"
export GH_CALLS_FILE="${gh_calls_file}"
export GH_THREADS_RESOLVED_FILE="${threads_resolved_file}"
export SANDBOX_REPO="${sandbox}"
export PATH="${stub_bin}:${PATH}"

finish_output="$(cd "${feature_worktree}" && "${TEST_ROOT}/scripts/task/finish-pr.sh" --resolve-threads 7)"

assert_output_contains 'PR #7 merged and cleaned up' "${finish_output}"
assert_contains 'resolveReviewThread' "${gh_calls_file}"
assert_contains 'pr merge 7 --merge' "${gh_calls_file}"
[[ ! -d "${feature_worktree}" ]] || fail "expected feature worktree to be removed"
git -C "${sandbox}" show-ref --verify --quiet refs/heads/codex/finish-cleanup &&
  fail "expected local feature branch to be deleted"
git -C "${sandbox}" ls-remote --exit-code --heads origin codex/finish-cleanup >/dev/null 2>&1 &&
  fail "expected remote feature branch to be deleted"
[[ "$(git -C "${sandbox}" branch --show-current)" == "develop" ]] || fail "expected sandbox worktree to stay on develop"
assert_file_exists "${sandbox}/finished.txt"
