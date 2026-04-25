# Harness Rollout

## 목표
- Codex가 이 저장소에서 구현 전후로 항상 같은 검증 루프를 돌릴 수 있게 만든다.

## 체크리스트
- [x] `AGENTS.md` 추가
- [x] `docs/` 문서 허브 추가
- [x] `test` profile 고정
- [x] `member/auth` 통합 테스트 추가
- [x] ArchUnit 구조 테스트 추가
- [x] CI에서 `-x test` 제거

## 결정 로그
- 기본 검증 명령은 `./gradlew test`로 단일화한다.
- 1차 대표 사용자 여정은 `member/auth`로 고정한다.
- 구조 규칙은 문서와 ArchUnit 테스트 둘 다로 유지한다.

## 후속 과제
- OAuth 실제 로그인 하네스 확장
- 문서 자동 검증 job 추가
- 품질 스코어 자동화
