# 2026 웨딧 기능 커버리지 매트릭스

## Provenance
- Normalized source: [2026-wedit-backend-features.csv](./generated/2026-wedit-backend-features.csv)
- Raw exports: [2026-wedit-feature-spec.csv](./sources/2026-wedit-feature-spec.csv), [2026-wedit-feature-spec-all.csv](./sources/2026-wedit-feature-spec-all.csv)

## Status Rules
- `implemented`: spec row가 현재 백엔드 contract와 테스트로 충족됨
- `planned`: domain/model 또는 회귀 테스트는 있지만 공개 contract가 아직 없음
- `gap`: 현재 코드/도메인에 직접 대응하는 contract가 없음
- `out_of_scope_for_backend`: 현재 기준으로 프론트 전용 항목

| feature_id | capability | domain_owner | status | implementation_evidence | test_evidence | next_step |
| --- | --- | --- | --- | --- | --- | --- |
| auth-google-login | authentication | member | gap | `src/main/java/com/wedit/backend/common/config/security/SecurityConfig.java`; `src/main/java/com/wedit/backend/common/oauth2/OAuth2UserService.java`; `src/main/java/com/wedit/backend/common/oauth2/OAuth2AuthenticationSuccessHandler.java` | - | Google OAuth 회원 플로우를 이메일 로그인과 분리해 명세대로 고정한다. |
| auth-signup-wedding-date | authentication | member | gap | `src/main/java/com/wedit/backend/api/member/controller/MemberController.java`; `src/main/java/com/wedit/backend/api/member/dto/MemberSignupRequestDTO.java`; `src/main/java/com/wedit/backend/api/member/entity/Member.java` | `src/test/java/com/wedit/backend/api/member/MemberFlowIntegrationTest.java` | 회원가입 요청과 member 모델에 예식일 필드를 추가한다. |
| nav-home-button | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 라우팅 책임으로 유지한다. |
| nav-invitation-button | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 라우팅 책임으로 유지한다. |
| nav-tour-journal-button | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 라우팅 책임으로 유지한다. |
| nav-my-page-button | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 라우팅 책임으로 유지한다. |
| home-quote-entry-button | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 화면 전환 책임으로 유지한다. |
| home-quote-card-summary | quote-home | quote | planned | `src/main/java/com/wedit/backend/api/vendor/entity/Vendor.java`; `src/main/java/com/wedit/backend/api/product/entity/ItemGroup.java` | `src/test/java/com/wedit/backend/api/product/entity/ItemGroupTest.java` | 홈 견적 요약 DTO와 집계 쿼리를 설계한다. |
| home-quote-weddinghall-list | quote-home | quote | planned | `src/main/java/com/wedit/backend/api/vendor/entity/WeddingHall.java`; `src/main/java/com/wedit/backend/api/vendor/repository/WeddingHallRepository.java` | `src/test/java/com/wedit/backend/api/vendor/entity/WeddingHallTest.java`; `src/test/java/com/wedit/backend/api/vendor/repository/VendorJpaTest.java` | 견적함 aggregate와 웨딩홀 목록 조회 API를 연결한다. |
| home-quote-studio-list | quote-home | quote | planned | `src/main/java/com/wedit/backend/api/vendor/entity/Studio.java`; `src/main/java/com/wedit/backend/api/vendor/repository/StudioRepository.java` | `src/test/java/com/wedit/backend/api/vendor/repository/VendorJpaTest.java` | 견적함 aggregate와 스튜디오 목록 조회 API를 연결한다. |
| home-quote-dress-list | quote-home | quote | planned | `src/main/java/com/wedit/backend/api/vendor/entity/Dress.java`; `src/main/java/com/wedit/backend/api/vendor/repository/DressRepository.java` | `src/test/java/com/wedit/backend/api/vendor/repository/VendorJpaTest.java` | 견적함 aggregate와 드레스 목록 조회 API를 연결한다. |
| home-quote-makeup-list | quote-home | quote | planned | `src/main/java/com/wedit/backend/api/vendor/entity/Makeup.java`; `src/main/java/com/wedit/backend/api/vendor/repository/MakeupRepository.java` | `src/test/java/com/wedit/backend/api/vendor/repository/VendorJpaTest.java` | 견적함 aggregate와 메이크업 목록 조회 API를 연결한다. |
| home-quote-soldout-status | quote-home | quote | planned | `src/main/java/com/wedit/backend/api/product/entity/OptionDetail.java` | `src/test/java/com/wedit/backend/api/product/entity/OptionDetailTest.java` | 견적 요약 응답에 품절/마감 상태를 포함한다. |
| home-quote-total | quote-home | quote | planned | `src/main/java/com/wedit/backend/api/product/entity/ItemGroup.java`; `src/main/java/com/wedit/backend/api/product/entity/Product.java`; `src/main/java/com/wedit/backend/api/product/entity/OptionDetail.java` | `src/test/java/com/wedit/backend/api/product/entity/ItemGroupTest.java`; `src/test/java/com/wedit/backend/api/product/entity/ProductTest.java`; `src/test/java/com/wedit/backend/api/product/entity/OptionDetailTest.java` | 선택된 업체 총합 계산 규칙을 read model에 추가한다. |
| home-recommendation-list | vendor-discovery | vendor | gap | - | - | 추천 기준과 에디터 큐레이션 저장 모델을 정의한다. |
| home-recommendation-detail-navigation | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 상세 이동 책임으로 유지한다. |
| home-search-hub-navigation | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 검색 허브 전환 책임으로 유지한다. |
| home-search-weddinghall | vendor-discovery | vendor | planned | `src/main/java/com/wedit/backend/api/vendor/entity/WeddingHall.java`; `src/main/java/com/wedit/backend/api/vendor/repository/WeddingHallRepository.java` | `src/test/java/com/wedit/backend/api/vendor/entity/WeddingHallTest.java`; `src/test/java/com/wedit/backend/api/vendor/repository/VendorJpaTest.java` | 웨딩홀 검색 필터와 목록 API를 설계한다. |
| home-search-studio | vendor-discovery | vendor | planned | `src/main/java/com/wedit/backend/api/vendor/entity/Studio.java`; `src/main/java/com/wedit/backend/api/vendor/repository/StudioRepository.java` | `src/test/java/com/wedit/backend/api/vendor/repository/VendorJpaTest.java` | 스튜디오 검색 필터와 목록 API를 설계한다. |
| home-search-dress | vendor-discovery | vendor | planned | `src/main/java/com/wedit/backend/api/vendor/entity/Dress.java`; `src/main/java/com/wedit/backend/api/vendor/repository/DressRepository.java` | `src/test/java/com/wedit/backend/api/vendor/repository/VendorJpaTest.java` | 드레스 검색 필터와 목록 API를 설계한다. |
| home-search-makeup | vendor-discovery | vendor | planned | `src/main/java/com/wedit/backend/api/vendor/entity/Makeup.java`; `src/main/java/com/wedit/backend/api/vendor/repository/MakeupRepository.java` | `src/test/java/com/wedit/backend/api/vendor/repository/VendorJpaTest.java` | 메이크업 검색 필터와 목록 API를 설계한다. |
| tour-journal-list-display | tour-journal | tour-journal | gap | - | - | 투어 기록 aggregate와 작성일 정렬 read model을 추가한다. |
| tour-journal-delete | tour-journal | tour-journal | gap | - | - | 투어 기록 삭제 command와 soft-delete 정책을 정의한다. |
| tour-journal-sketch-navigation | navigation | frontend | out_of_scope_for_backend | - | - | 프론트 화면 전환 책임으로 유지한다. |
| dress-sketch-base-body | dress-sketch | frontend | out_of_scope_for_backend | - | - | 현재는 프론트 스케치 에디터 자산으로 유지한다. |
| dress-sketch-line-style | dress-sketch | frontend | out_of_scope_for_backend | - | - | 현재는 프론트 스케치 에디터 자산으로 유지한다. |
| dress-sketch-neckline | dress-sketch | frontend | out_of_scope_for_backend | - | - | 현재는 프론트 스케치 에디터 자산으로 유지한다. |
| dress-sketch-material | dress-sketch | frontend | out_of_scope_for_backend | - | - | 현재는 프론트 스케치 에디터 자산으로 유지한다. |
| profile-basic-info | profile | member | planned | `src/main/java/com/wedit/backend/api/member/entity/Member.java` | `src/test/java/com/wedit/backend/api/member/MemberFlowIntegrationTest.java` | 마이 페이지 조회 API에 member 기본 정보를 노출한다. |
| profile-d-day | profile | member | gap | `src/main/java/com/wedit/backend/api/member/entity/Member.java` | - | member 모델에 예식일 필드와 D-day 계산 규칙을 추가한다. |
| invitation-create-entry | invitation | invitation | gap | - | - | 청첩장 draft 존재 여부와 생성 API를 설계한다. |
| invitation-preview | invitation | invitation | gap | - | - | 청첩장 preview read model을 정의한다. |
| invitation-theme | invitation | invitation | gap | - | - | 청첩장 aggregate와 theme 저장 규칙을 정의한다. |
| invitation-basic-info | invitation | invitation | gap | - | - | 청첩장 aggregate와 기본정보 저장 규칙을 정의한다. |
| invitation-greeting | invitation | invitation | gap | - | - | 청첩장 aggregate와 인사말 저장 규칙을 정의한다. |
| invitation-ceremony-datetime | invitation | invitation | gap | - | - | 청첩장 aggregate와 예식일시 저장 규칙을 정의한다. |
| invitation-venue | invitation | invitation | gap | - | - | 청첩장 aggregate와 예식장소 저장 규칙을 정의한다. |
| invitation-transportation | invitation | invitation | gap | - | - | 청첩장 aggregate와 교통수단 저장 규칙을 정의한다. |
| invitation-gallery | invitation | invitation | gap | - | - | 청첩장 aggregate와 갤러리 저장 규칙을 정의한다. |
| invitation-ending-locked-content | invitation | invitation | gap | - | - | 청첩장 aggregate와 엔딩 잠금 콘텐츠 규칙을 정의한다. |
| invitation-bank-account | invitation | invitation | gap | - | - | 청첩장 aggregate와 계좌번호 저장 규칙을 정의한다. |
| invitation-rsvp | invitation | invitation | gap | - | - | 청첩장 참석여부 write/read contract를 설계한다. |
| invitation-share | invitation | invitation | gap | - | - | 공유 token 또는 public read 정책을 설계한다. |
| invitation-bgm-partyroom-locked-content | invitation | invitation | gap | - | - | 청첩장 aggregate와 잠금 부가콘텐츠 규칙을 정의한다. |
| vendor-pricing-option-selection | vendor-pricing | vendor | planned | `src/main/java/com/wedit/backend/api/product/entity/ItemGroup.java`; `src/main/java/com/wedit/backend/api/product/entity/Product.java`; `src/main/java/com/wedit/backend/api/product/entity/OptionGroup.java`; `src/main/java/com/wedit/backend/api/product/entity/OptionDetail.java` | `src/test/java/com/wedit/backend/api/product/entity/ItemGroupTest.java`; `src/test/java/com/wedit/backend/api/product/entity/ProductTest.java`; `src/test/java/com/wedit/backend/api/product/entity/OptionGroupTest.java`; `src/test/java/com/wedit/backend/api/product/entity/OptionDetailTest.java` | 가격보기 read API와 옵션 조합 규칙을 노출한다. |
| vendor-quote-add | vendor-pricing | quote | gap | - | - | 견적함 aggregate와 add/remove command를 정의한다. |
| vendor-calendar-add | vendor-pricing | external-integration | gap | - | - | Google Calendar 연동 인증과 동기화 범위를 확정한다. |
| vendor-detail-main-photo | vendor-detail | vendor | implemented | `src/main/java/com/wedit/backend/api/vendor/controller/VendorController.java`; `src/main/java/com/wedit/backend/api/vendor/service/VendorService.java`; `src/main/java/com/wedit/backend/api/vendor/dto/VendorDetailResponseDTO.java` | `src/test/java/com/wedit/backend/api/vendor/VendorDetailCrudIntegrationTest.java` | 대표 이미지 응답 계약과 회귀 테스트를 유지한다. |
| vendor-detail-summary | vendor-detail | vendor | implemented | `src/main/java/com/wedit/backend/api/vendor/controller/VendorController.java`; `src/main/java/com/wedit/backend/api/vendor/service/VendorService.java`; `src/main/java/com/wedit/backend/api/vendor/dto/VendorSubtypeResponseDTO.java` | `src/test/java/com/wedit/backend/api/vendor/VendorDetailCrudIntegrationTest.java` | subtype summary 응답 계약과 회귀 테스트를 유지한다. |
| vendor-detail-gallery-and-location | vendor-detail | vendor | implemented | `src/main/java/com/wedit/backend/api/vendor/controller/VendorController.java`; `src/main/java/com/wedit/backend/api/vendor/service/VendorService.java`; `src/main/java/com/wedit/backend/api/vendor/dto/VendorMediaResponseDTO.java` | `src/test/java/com/wedit/backend/api/vendor/VendorDetailCrudIntegrationTest.java` | gallery/location 응답 계약과 회귀 테스트를 유지한다. |
