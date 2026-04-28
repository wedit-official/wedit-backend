# EXEC_PLAN: [feat] finish-pr cleanup 안정화

- Task slug: `finish-pr-cleanup-hardening`
- Base branch: `develop`
- Feature branch: `codex/finish-pr-cleanup-hardening`
- Worktree: `../wedit-backend-worktrees/finish-pr-cleanup-hardening`
- Port: `18080`
- Log dir: `../wedit-backend-logs/finish-pr-cleanup-hardening`
- Status: `verified`

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [x] docs/operations/pr-review-gate.md
- [x] docs/testing/codex-harness.md
- [x] docs/operations/obsidian-error-ledger.md

## Related Feature IDs
- [x] n/a-harness

## Doc Notes
- `finish-pr.sh` is the merge and cleanup owner after `verify-pr-ready.sh` passes.
- Cleanup must work from the `develop` worktree even though the pre-push hook only allows normal pushes from `codex/<slug>` branches.
- The repo uses an initialized `config` submodule, so feature worktree cleanup must tolerate submodule worktrees.
- Reproducible harness failures should be recorded in Obsidian Error Ledger or Current State.
- First Gradle verification failed because this new worktree had not initialized the `config` submodule; `git submodule update --init config` restored `application-local.yml`.
- A follow-up `clean` run hit a transient build directory deletion race; `./gradlew --stop` and rerunning the same command cleared it.
- After submodule initialization, `./gradlew clean check build --no-daemon` passed.
- Codex subagent review round 1 found that `--force --force` could delete dirty or locked feature worktrees. The script now blocks cleanup before merge when the feature worktree is dirty or locked, and tests cover both cases.
- After the round 1 fix, `bash scripts/tests/run.sh` passed and `./gradlew --stop && ./gradlew clean check build --no-daemon --rerun-tasks` passed.
- Codex subagent review round 2 found that a clean local feature worktree could contain unpushed commits. The script now requires local feature worktree HEAD to match the verified PR head SHA before merge, and the test covers this mismatch.
- After the round 2 fix, `bash scripts/tests/run.sh` passed and `./gradlew clean check build --no-daemon` passed.

## Goal
Harden the auto-finish PR cleanup path after PR #22 merged but failed during post-merge cleanup. The script must delete the merged remote feature branch despite local push hooks, remove feature worktrees that contain initialized submodules, and keep generated EXEC_PLAN files inside the feature worktree instead of dirtying the base `develop` checkout.

## Approach
Update `finish-pr.sh` to bypass local hooks only for the post-merge remote branch deletion and force-remove completed feature worktrees with submodules. Update task state helpers so linked worktrees share the base repo task state, then make `init-task.sh` create the EXEC_PLAN in the newly created feature worktree. Add script tests that reproduce the hook-blocked remote delete, submodule worktree removal, and EXEC_PLAN placement contract.

## Step Plan
1. Patch `finish-pr.sh` cleanup for remote branch deletion and submodule worktree removal.
2. Patch task state/worktree helpers so `init-task.sh` records the EXEC_PLAN in the feature worktree.
3. Extend script tests for the cleanup and EXEC_PLAN placement regressions.
4. Run `bash scripts/tests/run.sh`.
5. Run `./gradlew check build --no-daemon`.
6. Commit, create PR, complete subagent review loop, and merge after gates pass.

## Done Criteria
- `finish-pr.sh` can complete cleanup when local pre-push hooks would otherwise reject deletion from `develop`.
- `finish-pr.sh` can remove a completed feature worktree that has an initialized submodule.
- `finish-pr.sh` refuses to merge/cleanup when the local feature worktree is dirty or locked.
- `finish-pr.sh` refuses to merge/cleanup when the local feature worktree HEAD differs from the verified PR head.
- `init-task.sh` leaves no generated EXEC_PLAN file in the base `develop` checkout.
- Script tests cover the above regressions.
- `bash scripts/tests/run.sh` and `./gradlew check build --no-daemon` pass.
- PR is reviewed, pass-marked for the latest head, merged, and cleaned up.
