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
- [품질 스코어카드](./quality/scorecard.md)
- [현재 실행 계획](./exec-plans/active/harness-rollout.md)

## 기본 원칙
- 기본 검증 명령은 `./gradlew test` 입니다.
- 테스트는 `test` profile에서 외부 의존성 없이 통과해야 합니다.
- 작업에 사용한 관련 문서는 `EXEC_PLAN`의 `Related Docs`와 `Doc Notes`에 남겨야 합니다.
- 긴 설명보다 실행 가능한 계약을 문서에 남깁니다.
