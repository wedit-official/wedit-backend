# EXEC_PLAN: CI Docker Harness

- Task slug: `fix-ci-docker-harness`
- Base branch: `develop`
- Feature branch: `codex/fix-ci-docker-harness`
- Worktree: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-worktrees/ci-fix-20260428-lf`
- Port: `18080`
- Log dir: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-logs/fix-ci-docker-harness`
- Status: `completed-on-develop`

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [x] docs/testing/codex-harness.md
- [x] docs/quality/scorecard.md

## Doc Notes
- Recent `develop` push runs failed only after merge because the push workflow executes Docker build and PR/manual runs previously skipped that deploy path.
- The Dockerfile ran `./gradlew build -x test`, which still executes `check` and `shellTest`; that recursively runs Git/worktree harness tests inside a container image build.
- CI and local harness should verify Docker packaging explicitly, while the Dockerfile itself should only produce the runtime jar.

## Goal
- Make GitHub Actions staging/production CI catch Docker packaging failures before merge and pass on `develop` push.

## Approach
- Change Dockerfile builder stage from full Gradle `build` to jar packaging via `bootJar`.
- Run Docker image build for pull requests and push events in workflow, pushing only on push events.
- Add a harness contract test for Dockerfile/workflow behavior and a Gradle Docker build verification task that is required in CI.

## Step Plan
- Inspect failing GitHub Actions logs and identify the failing step.
- Update Dockerfile to avoid running Git-oriented shell harness inside image build.
- Update staging and production workflows so Docker image build runs on PRs too.
- Add harness coverage for the Docker/CI contract.
- Run local verification and push a fix branch.

## Done Criteria
- `./gradlew check build --no-daemon` passes locally.
- Docker build contract is represented in the harness.
- GitHub Actions for the fix PR runs both Gradle build and Docker image build successfully.

## Verification
- [x] `./gradlew check build --no-daemon`
- [x] Docker packaging contract test

## Completion
- Merged into `develop` through PR #14.
- Merge commit: `a31eb98`.
- The Docker image build is now represented by `ciDockerImageBuild` and CI workflow Docker build steps.
