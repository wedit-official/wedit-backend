# EXEC_PLAN: [feat] Gemini review-gated auto merge

- Task slug: `auto-merge-gemini-gate`
- Base branch: `develop`
- Feature branch: `codex/auto-merge-gemini-gate`
- Worktree: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-worktrees/auto-merge-gemini-gate`
- Port: `18080`
- Log dir: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-logs/auto-merge-gemini-gate`
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
- PR merge는 자동 review가 남긴 actionable comment/thread를 해결하고 `verify-pr-ready.sh`를 통과한 뒤에만 허용한다.
- 기존 gate는 review activity 존재 여부만 확인하므로, Gemini bot 리뷰 완료 여부를 명시적으로 기다리는 merge wrapper가 필요하다.
- 하네스/PR gate 실패는 재발 가능성이 있으면 Obsidian Error Ledger에 원인과 다음 체크포인트를 남긴다.

## Goal
- `merge까지 해줘`라고 매번 말하지 않아도 PR 생성 이후 Gemini bot review를 기다리고, review gate를 통과한 PR을 자동으로 merge/cleanup할 수 있는 하네스 명령을 추가한다.

## Approach
- 기존 `verify-pr-ready.sh`의 merge 차단 조건은 유지한다.
- 새 task script를 추가해 Gemini review/comment activity가 도착할 때까지 polling하고, 도착 후 `verify-pr-ready.sh`를 실행한 다음 merge commit 병합과 worktree/branch cleanup까지 수행한다.
- 사용자 또는 Codex는 review comment가 actionable이면 코드/테스트/문서에 반영한 뒤 같은 명령을 다시 실행한다. unresolved thread를 임의로 닫는 동작은 명시 옵션으로만 허용한다.
- PR 생성 문서와 PR body checklist를 자동 merge 흐름에 맞춰 갱신한다.

## Step Plan
1. 기존 PR gate, create-pr, shell test 구조를 확인한다.
2. Gemini review 대기와 merge/cleanup을 담당하는 task script를 추가한다.
3. PR body와 repo docs에 자동 merge 운영 방식을 문서화한다.
4. shell tests에 Gemini 대기, unresolved thread 차단, merge cleanup 계약을 추가한다.
5. `./gradlew check build --no-daemon`로 전체 검증한다.

## Done Criteria
- [x] Gemini bot review/comment activity가 없으면 merge하지 않고 기다리거나 timeout으로 실패한다.
- [x] Gemini review가 도착했더라도 unresolved review thread, requested changes, failing check, merge conflict가 있으면 merge하지 않는다.
- [x] gate 통과 시 merge commit 병합, develop 갱신, feature worktree 제거, local/remote feature branch 정리를 수행한다.
- [x] 자동 thread resolve는 명시 옵션에서만 가능하며, actionable comment를 코드/테스트/문서로 처리한 뒤 사용하는 것으로 문서화한다.
- [x] 관련 shell tests와 전체 Gradle 검증이 통과한다.

## Verification
- [x] `bash scripts/tests/run.sh`
- [x] `./gradlew check build --no-daemon`

## Result Notes
- 첫 `./gradlew check build --no-daemon`은 새 worktree의 `config` submodule이 초기화되지 않아 `LocalConfigContractTest`에서 실패했다.
- `git submodule update --init config` 후 동일 검증 명령이 통과했다.
