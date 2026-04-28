# 2026 웨딧 백엔드 기능 명세

## Provenance
- Raw export: [2026-wedit-feature-spec.csv](./sources/2026-wedit-feature-spec.csv), [2026-wedit-feature-spec-all.csv](./sources/2026-wedit-feature-spec-all.csv)
- Canonical backend rows: [2026-wedit-backend-features.csv](./generated/2026-wedit-backend-features.csv)
- Coverage matrix: [2026-wedit-backend-coverage-matrix.md](./2026-wedit-backend-coverage-matrix.md)
- Related Figma: [2026웨딧](https://www.figma.com/design/Y0Um16dutNEhJBB0YNhhSO/2026%EC%9B%A8%EB%94%A7?node-id=266-9801&p=f&viewport=-2078%2C-3756%2C0.71&t=BFjFkZfUL86sZcf9-0)

## 해석 원칙
- 이 문서는 화면 순서가 아니라 백엔드 capability 기준으로 재구성한 구현 기준 문서입니다.
- `backend_owned`는 백엔드가 직접 contract를 소유해야 하는 항목, `backend_shared`는 UI/외부 연동과 경계가 있지만 백엔드 계약이 필요한 항목, `out_of_scope_for_backend`는 현재 기준으로 프론트 전용 항목입니다.
- source of truth는 generated CSV와 coverage matrix입니다. 이 문서는 사람이 빠르게 의도를 이해하기 위한 capability 해설입니다.

## 1. Authentication And Onboarding
- Covered feature ids: `auth-google-login`, `auth-signup-wedding-date`
- 제품 명세는 Google OAuth 기반 로그인과 예식일 입력이 포함된 회원가입을 요구합니다.
- 현재 코드에는 이메일/비밀번호 + JWT 흐름과 OAuth2 인프라가 공존하지만, spec row 기준으로는 아직 완성되지 않았습니다.
- 구현 기준:
  - OAuth provider별 로그인 진입점과 성공 후 member upsert 규칙을 분리합니다.
  - 회원가입 입력은 `yyyy-mm-dd` 형식의 예식일/예정일을 명시적으로 수용해야 합니다.

## 2. Quote Home And Vendor Discovery
- Covered feature ids:
  - Quote summary: `home-quote-card-summary`, `home-quote-weddinghall-list`, `home-quote-studio-list`, `home-quote-dress-list`, `home-quote-makeup-list`, `home-quote-soldout-status`, `home-quote-total`
  - Discovery: `home-recommendation-list`, `home-search-weddinghall`, `home-search-studio`, `home-search-dress`, `home-search-makeup`
- 홈에서 요구하는 백엔드 책임은 “선택된 업체 집계”, “품절/마감 상태”, “추천 리스트”, “카테고리별 검색”입니다.
- 현재 엔티티 기준으로는 `vendor`, `itemGroup`, `product`, `optionGroup`, `optionDetail`가 read model의 기반이 될 수 있습니다.
- 아직 공개 API와 견적함 aggregate가 없으므로, 1차는 read model contract와 목록 필터링 규칙을 먼저 고정합니다.

## 3. Tour Journal Persistence
- Covered feature ids: `tour-journal-list-display`, `tour-journal-delete`
- 현재 명세에서 백엔드가 직접 소유하는 부분은 “기록한 리스트 표시”와 “삭제”입니다.
- 드레스 스케치 편집 자체(`dress-sketch-*`)는 저장 규칙이 정의되지 않아 프론트 전용으로 분류합니다.
- 구현 기준:
  - 투어 기록 aggregate에 작성일 정렬 규칙을 둡니다.
  - 삭제는 hard delete 대신 soft delete 여부를 먼저 결정합니다.

## 4. Profile And Invitation Data
- Covered feature ids:
  - Profile: `profile-basic-info`, `profile-d-day`
  - Invitation: `invitation-create-entry`, `invitation-preview`, `invitation-theme`, `invitation-basic-info`, `invitation-greeting`, `invitation-ceremony-datetime`, `invitation-venue`, `invitation-transportation`, `invitation-gallery`, `invitation-ending-locked-content`, `invitation-bank-account`, `invitation-rsvp`, `invitation-share`, `invitation-bgm-partyroom-locked-content`
- `member` 엔티티는 이름 중심 기본 프로필만 가지고 있고, D-day 계산에 필요한 예식일 필드는 아직 없습니다.
- 청첩장 영역은 현재 코드에 aggregate 자체가 없으므로 대부분 `gap`입니다.
- 구현 기준:
  - `member`와 `invitation` 책임을 분리합니다.
  - 미리보기/공유/참석여부는 UI 버튼이 아니라 read/write contract로 다룹니다.

## 5. Vendor Detail And Pricing
- Covered feature ids: `vendor-pricing-option-selection`, `vendor-quote-add`, `vendor-calendar-add`, `vendor-detail-main-photo`, `vendor-detail-summary`, `vendor-detail-gallery-and-location`
- 현재 `vendor`, `vendorMedia`, `itemGroup`, `product`, `optionGroup`, `optionDetail`는 업체 상세/가격보기 API의 기반이 됩니다.
- 아직 공개 detail/pricing API가 없고, 견적함 추가와 구글 캘린더 연동은 별도 aggregate/integration 설계가 필요합니다.
- 구현 기준:
  - 업체 상세는 summary, 대표 이미지, gallery/location을 한 번에 읽는 read model로 설계합니다.
  - 가격보기는 기본/추가 옵션 구조와 sold-out 상태를 함께 노출합니다.
  - 견적함 추가와 달력 연동은 command 성격이 강하므로 읽기 API와 분리합니다.

## 6. UI-Only Exclusions
- `out_of_scope_for_backend`: `nav-home-button`, `nav-invitation-button`, `nav-tour-journal-button`, `nav-my-page-button`, `home-quote-entry-button`, `home-recommendation-detail-navigation`, `home-search-hub-navigation`, `tour-journal-sketch-navigation`, `dress-sketch-base-body`, `dress-sketch-line-style`, `dress-sketch-neckline`, `dress-sketch-material`
- 이 항목들은 raw spec에는 남기되, 현재 하네스에서는 프론트 책임으로만 표시합니다.
- 이후 저장 규칙이나 백엔드 contract가 생기면 generated CSV 분류를 업데이트하고 matrix 상태를 다시 조정합니다.

## 현재 코드와의 차이
- `member/auth` 테스트 하네스는 이미 존재하지만, 제품 명세 기준으로는 Google OAuth와 예식일 입력을 아직 만족하지 않습니다.
- `vendor/product` 엔티티와 회귀 테스트는 풍부하지만, 사용자-facing 공개 API가 거의 없어서 대부분의 vendor-related feature는 `planned` 상태입니다.
- 청첩장과 투어일지 persistence는 아직 domain model 자체가 없으므로, 이 문서가 후속 구현 분해의 기준이 됩니다.
