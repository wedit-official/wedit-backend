# Wedit Backend Strict Workflow

이 저장소의 작업 규칙은 권고가 아니라 절대 규칙이다.

## 절대 규칙
- clone 이후 한 번은 `scripts/hooks/install-hooks.sh` 를 실행해 훅을 활성화한다.
- 절대 계획 없이 코드를 작성하지 마라.
- 구현은 항상 별도 worktree에서만 진행한다.
- `main`과 `develop` 브랜치에서 `src/`를 직접 수정하지 마라.
- 구현 전에 반드시 `AGENTS.md -> ARCHITECTURE.md -> docs/index.md -> 작업 관련 docs` 순서로 읽는다.
- test 작성과 검증 실행은 절대 건너뛸 수 없다.
- commit message는 반드시 Conventional Commits 형식을 따르고, `[feat] 설명` 형식을 사용한다.

## 1단계: EXEC_PLAN 생성
- 시작 명령: `scripts/task/init-task.sh <slug> "[title]"`
- 이 단계는 아래 자산을 모두 만든다.
  - `EXEC_PLAN`
  - worktree
  - 포트
  - 로그 디렉터리
- 생성된 `EXEC_PLAN`에는 아래가 비어 있으면 안 된다.
  - Required Reads
  - Related Docs
  - Related Feature IDs
  - Doc Notes
  - Goal
  - Approach
  - Step Plan
  - Done Criteria

## 2단계: Worktree에서 구현
- 생성된 worktree 디렉터리로 이동해서 작업한다.
- 구현 전 읽기 순서는 아래와 같다.
  - `AGENTS.md`
  - `ARCHITECTURE.md`
  - `docs/index.md`
  - 작업 관련 docs
- 읽은 문서는 반드시 `EXEC_PLAN`의 `Required Reads`, `Related Docs`, `Doc Notes`에 남긴다.
- 제품 기능 작업은 `EXEC_PLAN`의 `Related Feature IDs`에 실제 `feature_id`를 남긴다.
- 하네스/인프라 작업은 `Related Feature IDs`에 `n/a-harness`를 사용할 수 있다.
- main과 develop 브랜치에서는 `src/`를 직접 수정하지 않는다.

## 3단계: Test 작성
- 필수이며 절대 생략할 수 없다.
- 새 기능은 반드시 아래 테스트를 포함한다.
  - 정상 동작
  - 엣지 케이스
  - props 또는 입력 검증
- 버그 수정은 반드시 재현 테스트를 먼저 작성한다.
  - 실패
  - 수정
  - 통과
- 리팩터링은 기존 동작 확인 테스트를 포함한다.
- test가 준비되지 않으면 pre-commit 훅이 commit을 차단한다.

## 4단계: 검증 실행
- 필수이며 절대 생략할 수 없다.
- commit 전에 아래 검증이 모두 통과해야 한다.
  - lint
  - test
  - build
- 표준 검증 명령은 `./gradlew check build --no-daemon` 이다.
- 하나라도 실패하면 commit 하지 않는다.

## 5단계: Commit 및 Issue/PR
- 검증을 통과한 뒤 commit 한다.
- commit 이후 issue가 없으면 먼저 issue를 만들고, 이어서 `develop` 대상 PR을 생성한다.
- `scripts/task/create-issue.sh` 는 issue만 먼저 만들고 싶을 때 사용한다.
- `scripts/task/create-pr.sh` 는 issue가 없으면 먼저 생성한 뒤 PR을 만들고, PR 본문에서 `Closes #<issue>` 로 issue를 닫는다.
- 기본값으로 `scripts/task/create-pr.sh` 는 PR 생성 후 Gemini 자동 review를 기다리고, review gate가 통과하면 `scripts/task/finish-pr.sh` 로 merge와 정리를 이어서 수행한다.
- PR만 만들고 멈춰야 할 때만 `STRICT_AUTO_FINISH_PR=0 scripts/task/create-pr.sh` 를 사용한다.
- commit subject는 반드시 `[feat] 설명` 형식을 사용한다.

## 6단계: PR Review Gate 및 Merge
- PR 생성 후 자동 PR review가 끝날 때까지 기다린다.
- 자동 review가 남긴 모든 actionable comment와 review thread를 코드/테스트/문서에 반영하고 GitHub thread를 resolve 한다.
- 머지 전에는 반드시 `scripts/task/verify-pr-ready.sh <PR_NUMBER>` 를 실행한다.
- 이 게이트는 draft PR, requested changes, 미해결 review thread, pending/failing check, 충돌 상태를 차단한다.
- 실패 원인과 해결 과정은 repo `EXEC_PLAN`과 Obsidian `04 Errors/Error Ledger.md` 또는 `05 Handoffs/Current State.md`에 남긴다.
- 게이트 통과 후에는 `scripts/task/finish-pr.sh <PR_NUMBER>` 로 merge commit 병합, `develop` worktree 갱신, feature worktree 제거, local/remote branch 삭제를 한 번에 마무리한다.
- GitHub review thread를 코드/테스트/문서로 이미 처리했지만 unresolved 상태만 남은 경우에만 `scripts/task/finish-pr.sh --resolve-threads <PR_NUMBER>` 를 사용할 수 있다.

## 관련 문서
- 구조 요약: `ARCHITECTURE.md`
- 상세 문서 허브: `docs/index.md`
