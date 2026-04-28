# PR Review Gate

## 목적
- 자동 PR review가 남긴 피드백을 merge 전에 반드시 처리합니다.
- PR CI가 초록이어도 requested changes 또는 unresolved review thread가 있으면 merge하지 않습니다.

## 표준 절차
1. `scripts/task/create-pr.sh` 로 PR을 생성합니다.
2. 기본값으로 `create-pr.sh` 는 PR URL을 만든 뒤 `scripts/task/finish-pr.sh` 를 호출합니다.
3. `finish-pr.sh` 는 Gemini bot review/comment activity가 도착할 때까지 기다립니다.
4. 모든 actionable review comment를 코드, 테스트, 문서에 반영합니다.
5. 처리한 GitHub review thread는 resolve 합니다.
6. `finish-pr.sh` 가 `scripts/task/verify-pr-ready.sh <PR_NUMBER>` 를 실행합니다.
7. 스크립트가 통과한 PR만 merge commit 방식으로 `develop`에 병합하고 feature worktree와 local/remote branch를 정리합니다.

## 자동 완료 동작
- 기본 PR 생성 명령은 `scripts/task/create-pr.sh` 입니다.
- PR만 만들고 자동 대기/merge를 멈춰야 할 때는 `STRICT_AUTO_FINISH_PR=0 scripts/task/create-pr.sh` 를 사용합니다.
- Gemini bot login 판정은 `STRICT_REVIEW_BOT_REGEX` 로 조정할 수 있으며 기본값은 `[Gg]emini|gemini-code-assist` 입니다.
- Gemini bot activity 대기 시간은 `STRICT_REVIEW_BOT_TIMEOUT_SECONDS`, poll 간격은 `STRICT_REVIEW_BOT_INTERVAL_SECONDS` 로 조정합니다.
- 자동 thread resolve는 하지 않습니다. 이미 코드/테스트/문서로 처리한 thread만 `scripts/task/finish-pr.sh --resolve-threads <PR_NUMBER>` 로 정리할 수 있습니다.

## `verify-pr-ready.sh`가 막는 상태
- PR이 open 상태가 아님
- draft PR
- `CHANGES_REQUESTED`
- 자동 review/comment activity가 아직 없음
- Gemini bot review/comment activity가 아직 없음
- pending/failing/cancelled check
- unresolved PR review thread
- merge conflict 또는 blocked merge state

## 실패 기록
- 실패가 재발 가능하면 `EXEC_PLAN`에 원인과 처리 결과를 남깁니다.
- CI, Docker, 자동 review, GitHub 권한, 하네스 스크립트 문제는 Obsidian `04 Errors/Error Ledger.md`에 세부 기록을 남깁니다.
