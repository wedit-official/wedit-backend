# PR Review Gate

## 목적
- 자동 PR review가 남긴 피드백을 merge 전에 반드시 처리합니다.
- PR CI가 초록이어도 requested changes 또는 unresolved review thread가 있으면 merge하지 않습니다.

## 표준 절차
1. `scripts/task/create-pr.sh` 로 PR을 생성합니다.
2. 기본값으로 `create-pr.sh` 는 PR URL을 만든 뒤 `scripts/task/finish-pr.sh` 를 호출합니다.
3. `finish-pr.sh` 는 PR에 Gemini bot review/comment activity가 도착할 때까지 기다립니다.
4. 모든 actionable review comment를 코드, 테스트, 문서에 반영합니다.
5. Gemini가 추가 push를 다시 리뷰하지 않는 경우, Codex subagent review loop를 최대 3회 실행합니다.
6. subagent가 actionable finding을 찾으면 PR comment로 남기고 코드, 테스트, 문서에 반영합니다.
7. 처리한 GitHub review thread는 resolve 합니다.
8. `finish-pr.sh` 가 `scripts/task/verify-pr-ready.sh <PR_NUMBER>` 를 실행합니다.
9. 스크립트가 통과한 PR만 merge commit 방식으로 `develop`에 병합하고 feature worktree와 local/remote branch를 정리합니다.

## 자동 완료 동작
- 기본 PR 생성 명령은 `scripts/task/create-pr.sh` 입니다.
- PR만 만들고 자동 대기/merge를 멈춰야 할 때는 `STRICT_AUTO_FINISH_PR=0 scripts/task/create-pr.sh` 를 사용합니다.
- Gemini bot login 판정은 `STRICT_REVIEW_BOT_REGEX` 로 조정할 수 있으며 기본값은 `[Gg]emini|gemini-code-assist` 입니다.
- Gemini bot activity 대기 시간은 `STRICT_REVIEW_BOT_TIMEOUT_SECONDS`, poll 간격은 `STRICT_REVIEW_BOT_INTERVAL_SECONDS` 로 조정합니다.
- `verify-pr-ready.sh` 전후의 PR head SHA가 달라지면 merge하지 않고 review loop를 다시 실행합니다.
- merge는 `--match-head-commit`으로 검증 당시 PR head SHA에 고정합니다. gate 통과 뒤 새 commit이 push되면 merge가 실패하고 다시 review loop를 돌려야 합니다.
- 자동 thread resolve는 하지 않습니다. 이미 코드/테스트/문서로 처리한 thread만 `scripts/task/finish-pr.sh --resolve-threads <PR_NUMBER>` 로 정리할 수 있습니다.
- merge 이후 원격 feature branch 삭제는 local pre-push hook의 일반 push 제한을 우회해야 하므로 `finish-pr.sh`가 cleanup 삭제에만 `--no-verify`를 사용합니다.
- feature worktree에 initialized submodule이 있으면 일반 `git worktree remove`가 거부될 수 있으므로 cleanup은 완료된 feature worktree를 force remove 합니다. 단, merge 전 feature worktree가 clean이고 locked 상태가 아니며 local HEAD가 검증된 PR head SHA와 일치하는 경우에만 cleanup을 진행합니다.

## Codex Subagent Review Loop
- 목적은 Gemini에만 의존하지 않고 PR 변경을 한 번 더 독립적으로 검토하는 것입니다.
- 한 PR에서 최대 3회까지만 실행합니다.
- 각 회차는 `review -> PR comment -> fix -> verification -> push` 순서로 진행합니다.
- 3회 안에 해결되지 않으면 자동 merge하지 않고 handoff에 남깁니다.
- subagent가 no finding을 반환하면 PR comment에 no actionable finding과 남은 리스크를 남깁니다.
- merge 전에는 최신 PR head SHA에 대한 pass marker PR comment가 필요합니다.
- pass marker 형식은 `Codex Subagent Review Gate: PASS`와 `Head: <head_sha>`를 포함해야 합니다.

## `finish-pr.sh`가 막는 상태
- Gemini bot review/comment activity가 아직 없음
- 최신 PR head SHA에 대한 Codex subagent review pass marker가 없음
- `verify-pr-ready.sh` 전후로 PR head SHA가 바뀜

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
