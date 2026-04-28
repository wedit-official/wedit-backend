# Codex Harness

## 표준 검증 계약
- Codex의 기본 검증 명령은 `./gradlew check build --no-daemon` 입니다.
- 모든 테스트는 `test` profile에서 실행됩니다.
- 테스트는 로컬 MySQL, 실 OAuth client secret, 외부 네트워크 없이 통과해야 합니다.

## 포함 범위
- 엔티티/리포지토리 회귀 테스트
- `member/auth` 사용자 여정 통합 테스트
- ArchUnit 구조 가드레일 테스트
- Docker image packaging contract check. CI must build the image on PRs and pushes so deploy-only Dockerfile failures are caught before merge.
- PR review gate. Automated review activity must exist, actionable review threads must be resolved, and checks must pass before merge.
- Auto-finish PR gate. Gemini bot review activity must exist, Codex subagent review loop must run when Gemini does not review later pushes, and `finish-pr.sh` must pin merge to the verified PR head.

## 실패 시 확인 순서
1. `test` profile이 활성화됐는지 확인합니다.
2. datasource가 H2로 고정됐는지 확인합니다.
3. security bean과 OAuth placeholder 설정이 테스트 부팅을 막지 않는지 확인합니다.
4. JWT 헤더명과 secret, expiration 설정이 테스트 계약과 일치하는지 확인합니다.
5. 로컬과 CI 로그가 같은 실패 지점을 가리키는지 비교합니다.
6. PR/manual CI는 통과했지만 push CI만 실패하면 Docker build 단계가 PR 하네스에서 실행됐는지 확인합니다.
7. PR merge가 막히면 `scripts/task/finish-pr.sh <PR_NUMBER>` 를 다시 실행해 Gemini review 대기, requested changes, 미해결 review thread, pending/failing check 중 어디서 막혔는지 확인합니다.

## 금지 사항
- 테스트에서 실 DB 접속
- 테스트에서 실 OAuth provider 호출
- CI에서 `-x test`
- Dockerfile에서 전체 `build` 하네스를 다시 실행하는 재귀 빌드
- 자동 PR review의 actionable comment/thread를 unresolved 상태로 둔 채 merge
- Gemini bot review가 도착하기 전에 자동 merge
