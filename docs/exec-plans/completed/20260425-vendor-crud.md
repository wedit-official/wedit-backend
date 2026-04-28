# EXEC_PLAN: 업체 CRUD

- Task slug: `vendor-crud`
- Base branch: `feature/entity-domain`
- Feature branch: `codex/vendor-crud-domain`
- Worktree: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-worktrees/vendor-crud-domain`
- Port: `18080`
- Log dir: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-logs/vendor-crud`
- Status: `completed-on-develop`

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [x] docs/architecture/backend-map.md
- [x] docs/testing/codex-harness.md
- [x] docs/quality/scorecard.md

## Doc Notes
- `vendor` 도메인은 현재 `entity`와 `repository`, 일부 JPA/엔티티 테스트만 존재하고 `controller -> service -> repository -> entity` 계층에서 controller/service가 비어 있다.
- 공개 API는 controller 계층에서 시작하고 repository 접근은 service가 소유해야 하므로 업체 CRUD도 같은 계층 규칙을 따라야 한다.
- 테스트 계약은 `test` profile + H2 기반이며 외부 OAuth/DB 없이 통과해야 한다.
- `init-task.sh`가 생성한 `main` 기반 worktree에는 업체 엔티티 커밋이 없어 실제 구현 기준을 `feature/entity-domain`의 별도 worktree로 전환했다.

## Goal
- 업체 도메인에 대해 생성/단건 조회/목록 조회/수정/삭제(비활성화) CRUD API를 추가하고, 현재 엔티티 상속 구조가 실제 요청/응답과 영속화에서 정상 동작하는지 테스트로 보장한다.

## Approach
- `VendorCategory`를 기준으로 공통 필드와 업종별 필드를 함께 처리하는 DTO/서비스 계층을 추가한다.
- 엔티티에는 수정에 필요한 최소 변경 메서드를 추가하고, 삭제는 hard delete 대신 기존 `isActive` 플래그를 활용한 비활성화로 처리한다.
- 통합 테스트는 정상 흐름, 업종별/비활성화 엣지 케이스, 입력 검증 실패를 모두 포함한다.

## Step Plan
- 업체 엔티티/리포지토리와 기존 응답 패턴을 기준으로 CRUD API 계약을 설계한다.
- `vendor/controller`, `vendor/service`, `vendor/dto`를 추가하고 엔티티 수정 메서드를 구현한다.
- MockMvc 통합 테스트와 필요한 JPA/단위 테스트를 추가한다.
- `./gradlew check build --no-daemon`로 전체 검증을 실행한다.

## Done Criteria
- `api/vendor` 계층에 CRUD 엔드포인트가 존재하고 업종별 필드를 포함한 생성/조회/수정이 가능하다.
- 삭제 요청 시 업체는 비활성화되고 목록/조회 동작이 기대한 정책을 따른다.
- 정상 동작, 엣지 케이스, 입력 검증 테스트가 추가되고 전체 검증 명령이 통과한다.

## Completion
- Merged into `develop` through PR #10.
- Merge commit: `7a38b68`.
- Follow-up vendor detail work is tracked in `20260426-vendor-detail-crud-api.md`.
