# Obsidian Error Ledger

## 위치
- Vault: `<OBSIDIAN_PROJECT_ROOT>/Wedit`
- Error ledger: `04 Errors/Error Ledger.md`
- Handoff: `05 Handoffs/Current State.md`

## 기록 대상
- CI가 로컬 검증과 다르게 실패한 경우
- PR review gate가 requested changes 또는 unresolved thread로 막힌 경우
- Docker, 배포 manifest, GitHub Actions secret, GitHub 권한 문제
- 하네스 스크립트, hook, worktree, task state가 작업을 막은 경우

## 필수 필드
- Date
- Work / feature id
- Symptom
- Cause
- Fix
- Prevent next time
- 다음부터의 체크포인트

## 운영 규칙
- 실제로 발생한 실패만 기록합니다.
- 단순 요약으로 끝내지 말고 재현 단서와 다음 확인 순서를 남깁니다.
- repo 문서에는 실행 계약을, Obsidian에는 세션 맥락과 실패 원장을 남깁니다.
