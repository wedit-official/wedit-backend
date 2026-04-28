# Wedit Backend Harness Docs

이 문서는 Codex와 사람이 같은 검증 계약을 공유하기 위한 필수 문서 허브입니다.

## 읽기 순서
- `AGENTS.md`
- `ARCHITECTURE.md`
- `docs/index.md`
- 아래 문서 중 작업 관련 문서

## 문서 맵
- [아키텍처 맵](./architecture/backend-map.md)
- [Codex 테스트 하네스](./testing/codex-harness.md)
- [PR 리뷰 게이트](./operations/pr-review-gate.md)
- [Obsidian 오류 기록 규칙](./operations/obsidian-error-ledger.md)
- [품질 스코어카드](./quality/scorecard.md)
- [현재 실행 계획](./exec-plans/active/harness-rollout.md)

## 기본 원칙
- 기본 검증 명령은 `./gradlew check build --no-daemon` 입니다.
- 테스트는 `test` profile에서 외부 의존성 없이 통과해야 합니다.
- 작업에 사용한 관련 문서는 `EXEC_PLAN`의 `Related Docs`와 `Doc Notes`에 남겨야 합니다.
- 작업 대상 기능은 `EXEC_PLAN`의 `Related Feature IDs`에 남겨야 합니다. 하네스/인프라 작업은 `n/a-harness`를 사용합니다.
- PR 생성 후 기본 흐름은 Gemini 자동 review를 기다린 뒤 `scripts/task/finish-pr.sh`가 review gate, merge, cleanup을 이어서 수행하는 것입니다.
- PR merge 전에는 자동 PR review comment/thread를 모두 해결하고 `scripts/task/verify-pr-ready.sh`를 통과해야 합니다.
- 재발 가능한 CI/리뷰/하네스 실패는 Obsidian Error Ledger에 증상, 원인, 수정, 다음 체크포인트까지 남깁니다.
- 긴 설명보다 실행 가능한 계약을 문서에 남깁니다.
