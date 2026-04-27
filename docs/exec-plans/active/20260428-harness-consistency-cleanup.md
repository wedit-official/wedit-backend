# EXEC_PLAN: [feat] 하네스 일관성 정리

- Task slug: `harness-consistency-cleanup`
- Base branch: `develop`
- Feature branch: `codex/harness-consistency-cleanup`
- Worktree: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-worktrees/harness-consistency-cleanup`
- Port: `18082`
- Log dir: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-logs/harness-consistency-cleanup`
- Status: `verified`

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [x] docs/testing/codex-harness.md
- [x] docs/operations/pr-review-gate.md
- [x] docs/operations/obsidian-error-ledger.md
- [x] docs/quality/scorecard.md

## Related Feature IDs
- [x] n/a-harness

## Doc Notes
- Repo docs are the source of truth for implementation contracts, while Obsidian carries session state, handoff notes, and recurring failure context.
- The current `develop` harness already enforces Gradle check/build, shell workflow tests, Docker image packaging, and PR review readiness.
- The feature-spec harness exists on `codex/spec-harness-release`, but its `docs/specs`, `scripts/specs`, and `Related Feature IDs` flow are not fully present on `develop`.
- Obsidian notes are intentionally named `Wedit Backend Overview.md` and `2026 Wedit Backend Spec.md`; validator behavior should recognize those names instead of forcing alias note creation or renames.
- Several historical worktrees are dirty or not trivially merged; cleanup must preserve them and document their state instead of deleting user work.

## Goal
- Make the Wedit harness internally consistent across validator, repo docs, task scripts, feature-spec tracking, active/completed EXEC_PLANs, and Obsidian handoff state.
- Reintegrate feature-spec coverage validation into the current `develop` verification loop without changing application public API or domain code.
- Clean only safe stale worktree/task-state artifacts and preserve dirty worktrees.

## Approach
- Patch the reusable project harness validator so custom Wedit Obsidian note names satisfy the expected dashboard/spec template roles.
- Bring spec docs, normalization/verification scripts, and shell regression tests from `codex/spec-harness-release` onto the current `develop` harness.
- Add `Related Feature IDs` generation and enforcement, allowing `n/a-harness` for harness/infrastructure work.
- Move completed/stale active EXEC_PLANs into `docs/exec-plans/completed` and update docs/Obsidian state to describe the new operating model.
- Remove only the clean, already-merged PR review gate worktree and stale ignored task-state tied to completed work.

## Step Plan
- Add and wire the spec harness files and tests.
- Update task scripts, hooks, and shell tests for `Related Feature IDs`.
- Update repo docs and move completed EXEC_PLANs out of active.
- Patch the external project-harness validator alias logic.
- Update Obsidian Current State with preserved worktrees and cleanup results.
- Run spec harness, shell tests, full Gradle verification, and project harness validation.
- Commit with `[feat] 하네스 일관성 정리` and create a `develop` PR.

## Done Criteria
- `python3 scripts/specs/verify_feature_harness.py` passes.
- `bash scripts/tests/run.sh` passes and covers Related Feature IDs and spec-harness regressions.
- `./gradlew check build --no-daemon` passes.
- `project_harness.py validate ... --project-name Wedit` no longer fails because of Wedit custom Obsidian note names.
- Stale completed EXEC_PLANs are no longer under active.
- Dirty worktrees remain untouched and are documented in Obsidian handoff.

## Verification
- [x] `python3 scripts/specs/verify_feature_harness.py`
- [x] `bash scripts/tests/run.sh`
- [x] `python3 /Users/hyunwoo/.codex/skills/project-harness-obsidian/scripts/project_harness.py validate /Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-worktrees/harness-consistency-cleanup --project-name Wedit`
- [x] `python3 /Users/hyunwoo/.codex/skills/project-harness-obsidian/scripts/project_harness.py plan /Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-worktrees/harness-consistency-cleanup --project-name Wedit`
- [x] `./gradlew check build --no-daemon`
