# PR Review Gate

## 목적
- 자동 PR review가 남긴 피드백을 merge 전에 반드시 처리합니다.
- PR CI가 초록이어도 requested changes 또는 unresolved review thread가 있으면 merge하지 않습니다.

## 표준 절차
1. `scripts/task/create-pr.sh` 로 PR을 생성합니다.
2. 자동 PR review와 GitHub Actions check가 끝날 때까지 기다립니다.
3. 모든 actionable review comment를 코드, 테스트, 문서에 반영합니다.
4. 처리한 GitHub review thread는 resolve 합니다.
5. 머지 직전에 `scripts/task/verify-pr-ready.sh <PR_NUMBER>` 를 실행합니다.
6. 스크립트가 통과한 PR만 merge commit 방식으로 `develop`에 병합합니다.

## `verify-pr-ready.sh`가 막는 상태
- PR이 open 상태가 아님
- draft PR
- `CHANGES_REQUESTED`
- 자동 review/comment activity가 아직 없음
- pending/failing/cancelled check
- unresolved PR review thread
- merge conflict 또는 blocked merge state

## 실패 기록
- 실패가 재발 가능하면 `EXEC_PLAN`에 원인과 처리 결과를 남깁니다.
- CI, Docker, 자동 review, GitHub 권한, 하네스 스크립트 문제는 Obsidian `04 Errors/Error Ledger.md`에 세부 기록을 남깁니다.
