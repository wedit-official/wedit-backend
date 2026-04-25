# Wedit Backend Architecture

이 문서는 작업 시작 전에 읽는 아키텍처 요약 문서다.

## 현재 구조
- 공개 API 진입점은 현재 `MemberController` 중심이다.
- 도메인 코드는 `api/{member,agency,product,vendor}` 아래에 배치된다.
- 공통 관심사는 `common/config`, `common/oauth2`, `api/member/jwt` 에 분리되어 있다.

## 계층 규칙
- 기본 계층은 `controller -> service -> repository -> entity` 이다.
- controller는 repository를 직접 참조하지 않는다.
- repository 접근은 service 계층이 소유한다.
- entity는 controller를 참조하지 않는다.

## 작업 규칙
- 기능 구현 전에는 `AGENTS.md -> ARCHITECTURE.md -> docs/index.md -> 작업 관련 docs` 순서로 읽는다.
- `docs/index.md`는 상세 문서의 필수 진입점이다.
- 읽은 문서는 `EXEC_PLAN`의 문서 섹션에 남긴다.
- 구조 규칙은 문서와 테스트 둘 다로 유지한다.

## 상세 문서
- 문서 허브: [docs/index.md](./docs/index.md)
- 구조 상세: [docs/architecture/backend-map.md](./docs/architecture/backend-map.md)
- 테스트 계약: [docs/testing/codex-harness.md](./docs/testing/codex-harness.md)
