#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./testlib.sh
source "${SCRIPT_DIR}/testlib.sh"

workdir="$(mktemp -d)"
trap 'rm -rf "${workdir}"' EXIT

raw_csv="${workdir}/raw.csv"
generated_csv="${workdir}/generated.csv"
matrix_file="${workdir}/matrix.md"

cat > "${raw_csv}" <<'EOF'
세부 기능 (1),세부적인 구분,구분,부가설명,우선순위,구현난이도,백엔드 난이도,날짜,상태,상태 1
1.로그인,1.1 로그인,로그인,구글 OAuth를 통한 간편 로그인/회원가입 지원,4,중,중,,시작 전,시작 전
,1.2 회원가입,회원가입,yyyy-mm-dd 형식으로 본식일 또는 예정일 입력,4,,중,,시작 전,시작 전
2.내비게이션,2.1 홈,홈 버튼,홈 창 이동,4,하,하,,시작 전,시작 전
3.홈,3.3 검색,3.3.1 웨딩홀 검색,조건 기반 웨딩홀 탐색,4,,중,,시작 전,시작 전
EOF

python3 "${TEST_ROOT}/scripts/specs/normalize_feature_spec.py" \
  --raw "${raw_csv}" \
  --output "${generated_csv}"

assert_file_exists "${generated_csv}"
assert_contains 'auth-google-login' "${generated_csv}"
assert_contains 'auth-signup-wedding-date' "${generated_csv}"
assert_contains 'nav-home-button' "${generated_csv}"
assert_contains 'out_of_scope_for_backend' "${generated_csv}"
assert_contains 'home-search-weddinghall' "${generated_csv}"

cat > "${matrix_file}" <<'EOF'
# Matrix

| feature_id | capability | domain_owner | status | implementation_evidence | test_evidence | next_step |
| --- | --- | --- | --- | --- | --- | --- |
| auth-google-login | authentication | member | gap | - | - | Google OAuth 스펙과 현재 로그인 플로우를 분리한다. |
| auth-signup-wedding-date | authentication | member | gap | - | - | 회원가입 요청/엔티티에 예식일 필드를 추가한다. |
| nav-home-button | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 라우팅으로만 유지한다. |
| home-search-weddinghall | vendor-discovery | vendor | planned | src/main/java/com/wedit/backend/api/vendor/entity/WeddingHall.java | - | 검색 API와 필터 계약을 정의한다. |
EOF

python3 "${TEST_ROOT}/scripts/specs/verify_feature_harness.py" \
  --raw "${raw_csv}" \
  --generated "${generated_csv}" \
  --matrix "${matrix_file}" \
  --repo-root "${TEST_ROOT}"

cat > "${matrix_file}" <<'EOF'
# Matrix

| feature_id | capability | domain_owner | status | implementation_evidence | test_evidence | next_step |
| --- | --- | --- | --- | --- | --- | --- |
| auth-google-login | authentication | member | gap | - | - | Google OAuth 스펙과 현재 로그인 플로우를 분리한다. |
| auth-signup-wedding-date | authentication | member | gap | - | - | 회원가입 요청/엔티티에 예식일 필드를 추가한다. |
| nav-home-button | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 라우팅으로만 유지한다. |
EOF

assert_command_fails python3 "${TEST_ROOT}/scripts/specs/verify_feature_harness.py" \
  --raw "${raw_csv}" \
  --generated "${generated_csv}" \
  --matrix "${matrix_file}" \
  --repo-root "${TEST_ROOT}"

cat > "${matrix_file}" <<'EOF'
# Matrix

| feature_id | capability | domain_owner | status | implementation_evidence | test_evidence | next_step |
| --- | --- | --- | --- | --- | --- | --- |
| auth-google-login | authentication | member | implemented | src/main/java/com/wedit/backend/api/member/controller/MemberController.java | - | 구현 근거와 테스트를 함께 유지한다. |
| auth-signup-wedding-date | authentication | member | gap | - | - | 회원가입 요청/엔티티에 예식일 필드를 추가한다. |
| nav-home-button | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 라우팅으로만 유지한다. |
| home-search-weddinghall | vendor-discovery | vendor | planned | src/main/java/com/wedit/backend/api/vendor/entity/WeddingHall.java | - | 검색 API와 필터 계약을 정의한다. |
EOF

assert_command_fails python3 "${TEST_ROOT}/scripts/specs/verify_feature_harness.py" \
  --raw "${raw_csv}" \
  --generated "${generated_csv}" \
  --matrix "${matrix_file}" \
  --repo-root "${TEST_ROOT}"
