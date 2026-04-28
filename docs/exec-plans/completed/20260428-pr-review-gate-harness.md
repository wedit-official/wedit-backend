# EXEC_PLAN: PR Review Gate Harness

- Task slug: `pr-review-gate-harness`
- Base branch: `develop`
- Feature branch: `codex/pr-review-gate-harness`
- Worktree: `<WORKTREE_BASE>/pr-review-gate-harness`
- Port: `18080`
- Log dir: `<LOG_BASE>/pr-review-gate-harness`
- Status: `completed-on-develop`

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [x] docs/testing/codex-harness.md
- [x] docs/quality/scorecard.md

## Doc Notes
- Existing harness covered Docker packaging but did not provide a merge-time gate for automated PR review comments.
- Obsidian already has `04 Errors/Error Ledger.md`; this task makes PR review/CI failures part of the explicit operating contract.

## Goal
- Add a strict merge-time review gate so PRs are merged only after automated review feedback and CI checks are resolved.
- Document Obsidian error-ledger expectations for detailed CI/review/harness failures.

## Approach
- Add `scripts/task/verify-pr-ready.sh` to check GitHub PR state, review activity, requested changes, unresolved review threads, checks, and merge state.
- Add shell-test coverage and PR body checklist text so the new gate remains part of the harness.
- Update repo docs and Obsidian handoff/error ledger rules.

## Step Plan
- Update task defaults and PR creation body for `develop` and review-gate expectations.
- Add the review-gate script and shell tests.
- Update harness docs, quality scorecard, and Obsidian notes.
- Run `./gradlew check build --no-daemon`.

## Done Criteria
- `scripts/task/verify-pr-ready.sh` blocks missing automated review activity, requested changes, unresolved review threads, bad checks, and blocked merge state.
- PR body includes the review-gate checklist.
- Repo docs and Obsidian notes describe detailed failure logging.
- Full local verification passes.

## Verification
- [x] `./gradlew check build --no-daemon`
- [x] PR review gate shell tests

## Completion
- Merged into `develop` through PR #16.
- Merge commit: `107f40c`.
- The clean merged worktree may be removed during harness consistency cleanup.
