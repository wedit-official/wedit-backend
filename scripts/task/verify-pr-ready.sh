#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

repo="$(repo_root)"
pr_ref="${1:-}"

if [[ -z "${pr_ref}" ]]; then
  branch="$(git -C "${repo}" branch --show-current)"
  [[ "${branch}" =~ ^codex/([a-z0-9][a-z0-9-]*)$ ]] || fail "usage: verify-pr-ready.sh <pr-number|pr-url|branch>; current branch is not codex/<slug>: ${branch}"
  pr_ref="${branch}"
fi

command -v gh >/dev/null 2>&1 || fail "gh CLI is required to verify PR readiness"
command -v jq >/dev/null 2>&1 || fail "jq is required to parse GitHub PR readiness data"

pr_json="$(gh pr view "${pr_ref}" --json number,url,state,isDraft,reviewDecision,reviews,comments,id,mergeStateStatus)"
pr_number="$(jq -r '.number' <<<"${pr_json}")"
pr_url="$(jq -r '.url' <<<"${pr_json}")"
state="$(jq -r '.state' <<<"${pr_json}")"
is_draft="$(jq -r '.isDraft' <<<"${pr_json}")"
review_decision="$(jq -r '.reviewDecision // ""' <<<"${pr_json}")"
review_activity_count="$(jq -r '((.reviews // []) | length) + ((.comments // []) | length)' <<<"${pr_json}")"

[[ "${state}" == "OPEN" ]] || fail "PR #${pr_number} is not open: ${state}"
[[ "${is_draft}" == "false" ]] || fail "PR #${pr_number} is still draft"
[[ "${review_decision}" != "CHANGES_REQUESTED" ]] || fail "PR #${pr_number} has requested changes; address review feedback before merge"
[[ "${review_activity_count}" -gt 0 ]] || fail "PR #${pr_number} has no review/comment activity yet; wait for the automated PR review before merge"

if [[ "${STRICT_SKIP_PR_CHECKS:-0}" != "1" ]]; then
  gh pr checks "${pr_number}" --watch --fail-fast --interval "${STRICT_PR_CHECK_INTERVAL:-10}"
fi

pr_node_id="$(jq -r '.id' <<<"${pr_json}")"
review_threads_query='
query($id: ID!) {
  node(id: $id) {
    ... on PullRequest {
      reviewThreads(first: 100) {
        pageInfo {
          hasNextPage
        }
        nodes {
          isResolved
          path
          line
          comments(first: 1) {
            nodes {
              author {
                login
              }
              url
            }
          }
        }
      }
    }
  }
}
'

review_threads_json="$(gh api graphql -f id="${pr_node_id}" -f query="${review_threads_query}")"

has_next_page="$(jq -r '.data.node.reviewThreads.pageInfo.hasNextPage' <<<"${review_threads_json}")"
[[ "${has_next_page}" == "false" ]] || fail "PR #${pr_number} has more than 100 review threads; inspect and resolve them manually before merge"

unresolved_threads="$(
  jq -r '.data.node.reviewThreads.nodes[] | select(.isResolved == false) | "- \(.path):\(.line // 0) \(.comments.nodes[0].author.login // "unknown") \(.comments.nodes[0].url // "")"' <<<"${review_threads_json}"
)"

if [[ -n "${unresolved_threads}" ]]; then
  {
    echo "Error: PR #${pr_number} has unresolved PR review threads:"
    echo "${unresolved_threads}"
  } >&2
  exit 1
fi

merge_state="$(jq -r '.mergeStateStatus // ""' <<<"${pr_json}")"
case "${merge_state}" in
  DIRTY|BLOCKED)
    fail "PR #${pr_number} is not mergeable: ${merge_state}"
    ;;
esac

printf 'PR #%s is ready to merge: %s\n' "${pr_number}" "${pr_url}"
