# Backend Map

## 현재 구조
- 현재 공개 API 엔드포인트는 `MemberController`가 담당하는 회원가입, 로그인, 토큰 재발급, 회원 탈퇴입니다.
- 도메인 패키지는 `api/{member,agency,product,vendor}` 아래에 배치되어 있습니다.
- 공통 관심사는 `common/config`, `common/oauth2`, `api/member/jwt`에 분리되어 있습니다.

## 계층 규칙
- 기본 계층은 `controller -> service -> repository -> entity` 입니다.
- controller는 repository를 직접 참조하지 않습니다.
- repository 접근은 service 계층이 소유합니다.
- entity는 controller를 참조하지 않습니다.

## Cross-Cutting
- 보안, JWT, OAuth2는 비즈니스 도메인 위에 놓인 cross-cutting 영역입니다.
- cross-cutting 코드는 repository를 직접 붙잡기보다 service를 통해 도메인 상태를 조회합니다.

## Codex 메모
- 구조 규칙은 문서만이 아니라 ArchUnit 테스트로도 검증됩니다.
- 새로운 공개 API를 추가할 때는 이 문서와 구조 테스트를 함께 갱신합니다.
