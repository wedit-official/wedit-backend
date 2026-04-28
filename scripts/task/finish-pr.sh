#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

usage() {
  cat >&2 <<'EOF'
usage: finish-pr.sh [--resolve-threads] [--skip-review-bot-wait] [pr-number|pr-url|branch]

Waits for the configured automated review bot, verifies that PR checks and
review threads are ready, merges the PR, updates the local develop worktree,
and removes the feature worktree and branches.

Use --resolve-threads only after actionable review comments have been addressed
in code, tests, or docs.

Environment:
  STRICT_REVIEW_BOT_REGEX              Regex for the required bot login.
                                      Default: [Gg]emini|gemini-code-assist
  STRICT_REVIEW_BOT_TIMEOUT_SECONDS    Default: 1800
  STRICT_REVIEW_BOT_INTERVAL_SECONDS   Default: 30
  STRICT_SKIP_REVIEW_BOT_WAIT          Set to 1 to skip the bot wait.
EOF
  exit 1
}

resolve_threads=false
skip_review_bot_wait=false
pr_ref=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --resolve-threads)
      resolve_threads=true
      shift
      ;;
    --skip-review-bot-wait)
      skip_review_bot_wait=true
      shift
      ;;
    -h|--help)
      usage
      ;;
    *)
      [[ -z "${pr_ref}" ]] || usage
      pr_ref="$1"
      shift
      ;;
  esac
done

repo="$(repo_root)"
if [[ -z "${pr_ref}" ]]; then
  branch="$(git -C "${repo}" branch --show-current)"
  [[ "${branch}" =~ ^codex/([a-z0-9][a-z0-9-]*)$ ]] || fail "usage: finish-pr.sh [--resolve-threads] <pr-number|pr-url|branch>; current branch is not codex/<slug>: ${branch}"
  pr_ref="${branch}"
fi

command -v gh >/dev/null 2>&1 || fail "gh CLI is required to finish PRs"
command -v jq >/dev/null 2>&1 || fail "jq is required to parse GitHub PR data"

find_worktree_for_branch() {
  local branch="$1"

  git -C "${repo}" worktree list --porcelain | awk -v target="refs/heads/${branch}" '
    /^worktree / {
      worktree = substr($0, 10)
      next
    }
    /^branch / && substr($0, 8) == target {
      print worktree
      exit
    }
  '
}

review_bot_has_activity() {
  local pr="$1"
  local review_bot_regex="${STRICT_REVIEW_BOT_REGEX:-[Gg]emini|gemini-code-assist}"

  gh pr view "${pr}" --json reviews,comments |
    jq -e --arg regex "${review_bot_regex}" '
      [
        (.reviews // [])[]?.author.login,
        (.comments // [])[]?.author.login
      ]
      | map(select(. != null))
      | any(test($regex))
    ' >/dev/null
}

wait_for_review_bot_activity() {
  local pr="$1"
  local timeout_seconds="${STRICT_REVIEW_BOT_TIMEOUT_SECONDS:-1800}"
  local interval_seconds="${STRICT_REVIEW_BOT_INTERVAL_SECONDS:-30}"
  local review_bot_regex="${STRICT_REVIEW_BOT_REGEX:-[Gg]emini|gemini-code-assist}"
  local start now elapsed

  start="$(date '+%s')"
  while true; do
    if review_bot_has_activity "${pr}"; then
      printf 'Automated review bot activity detected for PR %s (regex: %s)\n' "${pr}" "${review_bot_regex}"
      return 0
    fi

    now="$(date '+%s')"
    elapsed=$((now - start))
    if [[ "${elapsed}" -ge "${timeout_seconds}" ]]; then
      fail "timed out waiting for automated review bot activity on PR ${pr} (regex: ${review_bot_regex})"
    fi

    printf 'Waiting for automated review bot activity on PR %s (elapsed %ss/%ss)\n' "${pr}" "${elapsed}" "${timeout_seconds}"
    sleep "${interval_seconds}"
  done
}

resolve_review_threads() {
  local pr_node_id="$1"
  local pr_number="$2"
  local query threads thread_id

  query='
query($id: ID!) {
  node(id: $id) {
    ... on PullRequest {
      reviewThreads(first: 100) {
        pageInfo {
          hasNextPage
        }
        nodes {
          id
          isResolved
        }
      }
    }
  }
}
'

  threads="$(gh api graphql -f id="${pr_node_id}" -f query="${query}")"
  [[ "$(jq -r '.data.node.reviewThreads.pageInfo.hasNextPage' <<<"${threads}")" == "false" ]] ||
    fail "PR #${pr_number} has more than 100 review threads; resolve them manually before merge"

  while IFS= read -r thread_id; do
    [[ -n "${thread_id}" ]] || continue
    gh api graphql \
      -f threadId="${thread_id}" \
      -f query='mutation($threadId: ID!) { resolveReviewThread(input: { threadId: $threadId }) { thread { id isResolved } } }' \
      >/dev/null
  done < <(jq -r '.data.node.reviewThreads.nodes[] | select(.isResolved == false) | .id' <<<"${threads}")
}

pr_json="$(gh pr view "${pr_ref}" --json number,url,state,id,headRefName,baseRefName)"
pr_number="$(jq -r '.number' <<<"${pr_json}")"
pr_url="$(jq -r '.url' <<<"${pr_json}")"
pr_node_id="$(jq -r '.id' <<<"${pr_json}")"
head_branch="$(jq -r '.headRefName' <<<"${pr_json}")"
base_branch="$(jq -r '.baseRefName' <<<"${pr_json}")"
state="$(jq -r '.state' <<<"${pr_json}")"
remote_name="${STRICT_REMOTE_NAME:-origin}"

[[ "${state}" == "OPEN" ]] || fail "PR #${pr_number} is not open: ${state}"
[[ "${base_branch}" == "develop" ]] || fail "PR #${pr_number} must target develop, found: ${base_branch}"
[[ "${head_branch}" =~ ^codex/[a-z0-9][a-z0-9-]*$ ]] || fail "PR #${pr_number} head branch must match codex/<slug>: ${head_branch}"

develop_worktree="$(find_worktree_for_branch "develop")"
[[ -n "${develop_worktree}" ]] || fail "local develop worktree was not found"
[[ -z "$(git -C "${develop_worktree}" status --short)" ]] ||
  fail "develop worktree has uncommitted changes; clean it before finishing PR #${pr_number}: ${develop_worktree}"

if [[ "${skip_review_bot_wait}" != "true" && "${STRICT_SKIP_REVIEW_BOT_WAIT:-0}" != "1" ]]; then
  wait_for_review_bot_activity "${pr_number}"
fi

if [[ "${resolve_threads}" == "true" ]]; then
  resolve_review_threads "${pr_node_id}" "${pr_number}"
fi

"${SCRIPT_DIR}/verify-pr-ready.sh" "${pr_number}"

feature_worktree="$(find_worktree_for_branch "${head_branch}" || true)"

gh pr merge "${pr_number}" --merge

if git -C "${develop_worktree}" ls-remote --exit-code --heads "${remote_name}" "${head_branch}" >/dev/null 2>&1; then
  git -C "${develop_worktree}" push "${remote_name}" --delete "${head_branch}" >/dev/null
fi

git -C "${develop_worktree}" fetch "${remote_name}" develop --prune
git -C "${develop_worktree}" checkout develop >/dev/null
git -C "${develop_worktree}" pull --ff-only "${remote_name}" develop

if [[ -n "${feature_worktree}" && "${feature_worktree}" != "${develop_worktree}" ]]; then
  git -C "${develop_worktree}" worktree remove "${feature_worktree}"
fi

if git -C "${develop_worktree}" show-ref --verify --quiet "refs/heads/${head_branch}"; then
  git -C "${develop_worktree}" branch -d "${head_branch}"
fi

printf 'PR #%s merged and cleaned up: %s\n' "${pr_number}" "${pr_url}"
printf 'Develop worktree: %s\n' "${develop_worktree}"
