# EXEC_PLAN: Add local datasource config

- Task slug: `local-config-datasource`
- Base branch: `develop`
- Feature branch: `codex/local-config-datasource`
- Worktree: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-worktrees/local-config-datasource`
- Port: `18081`
- Log dir: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-logs/local-config-datasource`
- Status: `completed`

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [x] docs/architecture/backend-map.md
- [x] docs/testing/codex-harness.md
- [x] docs/exec-plans/active/harness-rollout.md

## Doc Notes
- `build.gradle` copies `config/src/main/resources/application*.yml` into `src/main/resources` through `copyConfig` before resource processing.
- The default active profile is `${SPRING_PROFILES_ACTIVE:local}`.
- Tests must avoid real MySQL, OAuth providers, and external services; config contract tests should inspect files without opening a database connection.

## Goal
Restore local boot configuration by adding a `local` profile datasource file to the config submodule.

## Approach
Add `config/src/main/resources/application-local.yml` with local MySQL datasource settings and environment-variable overrides. Add a config contract test that verifies the local datasource keys exist and remain overrideable without starting the Spring context.

## Step Plan
1. Initialize the task worktree and config submodule.
2. Add local datasource YAML under the config submodule.
3. Add focused config contract tests for normal datasource presence, required input validation, and environment override support.
4. Run `copyConfig`, focused tests, and full Gradle verification.

## Done Criteria
- `application-local.yml` exists in the config submodule.
- `copyConfig` copies the local profile YAML into `src/main/resources`.
- Focused local config contract tests pass.
- `./gradlew check build --no-daemon` passes before commit or PR work.

## Verification
- `./gradlew copyConfig --no-daemon`: passed, `src/main/resources/application-local.yml` was copied.
- `./gradlew test --tests com.wedit.backend.config.LocalConfigContractTest --no-daemon`: passed.
- `./gradlew check build --no-daemon`: passed. Docker image build was skipped by the existing harness because Docker is not available locally.
- Review follow-up: removed the hardcoded local DB password fallback and verified the config contract rejects that leaked default.
- `./gradlew copyConfig --no-daemon && ./gradlew test --tests com.wedit.backend.config.LocalConfigContractTest --no-daemon`: passed after review follow-up.
- `./gradlew check build --no-daemon`: passed after review follow-up. Docker image build was skipped by the existing harness because Docker is not available locally.
