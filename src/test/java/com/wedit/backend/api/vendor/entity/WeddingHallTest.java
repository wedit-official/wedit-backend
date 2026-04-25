package com.wedit.backend.api.vendor.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Vendor 엔티티 단위 테스트")
class WeddingHallTest {

    // ────────────────────────────────────────────────────────
    // 공통 빌더 헬퍼
    // ────────────────────────────────────────────────────────

    private WeddingHall buildDefaultWeddingHall() {
        return WeddingHall.builder()
                .name("그랜드 웨딩홀")
                .region("서울")
                .fullAddress("서울특별시 강남구 테헤란로 1")
                .addressDetail("2층")
                .contactInfo("02-1234-5678")
                .latitude(37.5665)
                .longitude(126.9780)
                .kakaoMapUrl("https://map.kakao.com/link/1")
                .website("https://grand-wedding.com")
                .instagramUrl("https://instagram.com/grand_wedding")
                .description("서울 중심부에 위치한 럭셔리 웨딩홀입니다.")
                .capacity(500)
                .hallCount(3)
                .mealAvailable(true)
                .parkingAvailable(true)
                .build();
    }

    // ────────────────────────────────────────────────────────
    // WeddingHall
    // ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("WeddingHall 엔티티")
    class WeddingHallTests {

        @Test
        @DisplayName("빌더로 생성 시 모든 필드가 정상적으로 설정된다")
        void builder_setsAllFields() {
            WeddingHall hall = buildDefaultWeddingHall();

            assertThat(hall.getName()).isEqualTo("그랜드 웨딩홀");
            assertThat(hall.getRegion()).isEqualTo("서울");
            assertThat(hall.getFullAddress()).isEqualTo("서울특별시 강남구 테헤란로 1");
            assertThat(hall.getAddressDetail()).isEqualTo("2층");
            assertThat(hall.getContactInfo()).isEqualTo("02-1234-5678");
            assertThat(hall.getLatitude()).isEqualTo(37.5665);
            assertThat(hall.getLongitude()).isEqualTo(126.9780);
            assertThat(hall.getKakaoMapUrl()).isEqualTo("https://map.kakao.com/link/1");
            assertThat(hall.getWebsite()).isEqualTo("https://grand-wedding.com");
            assertThat(hall.getInstagramUrl()).isEqualTo("https://instagram.com/grand_wedding");
            assertThat(hall.getDescription()).isEqualTo("서울 중심부에 위치한 럭셔리 웨딩홀입니다.");
            assertThat(hall.getCapacity()).isEqualTo(500);
            assertThat(hall.getHallCount()).isEqualTo(3);
            assertThat(hall.isMealAvailable()).isTrue();
            assertThat(hall.isParkingAvailable()).isTrue();
        }

        @Test
        @DisplayName("생성 직후 isActive 는 true 이다")
        void isActive_isTrueOnCreation() {
            WeddingHall hall = buildDefaultWeddingHall();

            assertThat(hall.isActive()).isTrue();
        }

        @Test
        @DisplayName("getVendorCategory() 는 WEDDING_HALL 을 반환한다")
        void getVendorCategory_returnsWeddingHall() {
            WeddingHall hall = buildDefaultWeddingHall();

            assertThat(hall.getVendorCategory()).isEqualTo(VendorCategory.WEDDING_HALL);
        }

        @Test
        @DisplayName("deactivate() 호출 후 isActive 는 false 가 된다")
        void deactivate_setsIsActiveFalse() {
            WeddingHall hall = buildDefaultWeddingHall();

            hall.deactivate();

            assertThat(hall.isActive()).isFalse();
        }

        @Test
        @DisplayName("instagramUrl 이 null 이어도 정상 생성된다 (선택 필드)")
        void instagramUrl_allowsNull() {
            WeddingHall hall = WeddingHall.builder()
                    .name("심플 웨딩홀")
                    .region("부산")
                    .fullAddress("부산광역시 해운대구 해운대로 1")
                    .instagramUrl(null)
                    .capacity(200)
                    .hallCount(1)
                    .mealAvailable(false)
                    .parkingAvailable(true)
                    .build();

            assertThat(hall.getInstagramUrl()).isNull();
            assertThat(hall.getName()).isEqualTo("심플 웨딩홀");
        }

        @Test
        @DisplayName("생성 직후 itemGroups 와 mediaList 는 빈 리스트다")
        void collections_areEmptyOnCreation() {
            WeddingHall hall = buildDefaultWeddingHall();

            assertThat(hall.getItemGroups()).isEmpty();
            assertThat(hall.getMediaList()).isEmpty();
        }
    }

    // ────────────────────────────────────────────────────────
    // Studio
    // ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Studio 엔티티")
    class StudioTests {

        private Studio buildDefaultStudio() {
            return Studio.builder()
                    .name("아트 스튜디오")
                    .region("서울")
                    .fullAddress("서울특별시 마포구 홍익로 5")
                    .addressDetail("3층")
                    .contactInfo("02-9876-5432")
                    .latitude(37.5500)
                    .longitude(126.9230)
                    .kakaoMapUrl("https://map.kakao.com/link/2")
                    .website("https://art-studio.com")
                    .instagramUrl("https://instagram.com/art_studio")
                    .description("감성적인 웨딩 촬영 전문 스튜디오입니다.")
                    .isOutdoor(true)
                    .photographerCount(5)
                    .shootingStyle("내추럴 & 빈티지")
                    .build();
        }

        @Test
        @DisplayName("빌더로 생성 시 모든 필드가 정상적으로 설정된다")
        void builder_setsAllFields() {
            Studio studio = buildDefaultStudio();

            assertThat(studio.getName()).isEqualTo("아트 스튜디오");
            assertThat(studio.getRegion()).isEqualTo("서울");
            assertThat(studio.getFullAddress()).isEqualTo("서울특별시 마포구 홍익로 5");
            assertThat(studio.getAddressDetail()).isEqualTo("3층");
            assertThat(studio.isOutdoor()).isTrue();
            assertThat(studio.getPhotographerCount()).isEqualTo(5);
            assertThat(studio.getShootingStyle()).isEqualTo("내추럴 & 빈티지");
        }

        @Test
        @DisplayName("생성 직후 isActive 는 true 이다")
        void isActive_isTrueOnCreation() {
            Studio studio = buildDefaultStudio();

            assertThat(studio.isActive()).isTrue();
        }

        @Test
        @DisplayName("getVendorCategory() 는 STUDIO 를 반환한다")
        void getVendorCategory_returnsStudio() {
            Studio studio = buildDefaultStudio();

            assertThat(studio.getVendorCategory()).isEqualTo(VendorCategory.STUDIO);
        }

        @Test
        @DisplayName("deactivate() 호출 후 isActive 는 false 가 된다")
        void deactivate_setsIsActiveFalse() {
            Studio studio = buildDefaultStudio();

            studio.deactivate();

            assertThat(studio.isActive()).isFalse();
        }

        @Test
        @DisplayName("instagramUrl 이 null 이어도 정상 생성된다 (선택 필드)")
        void instagramUrl_allowsNull() {
            Studio studio = Studio.builder()
                    .name("미니멀 스튜디오")
                    .region("경기")
                    .fullAddress("경기도 수원시 팔달구 1")
                    .instagramUrl(null)
                    .isOutdoor(false)
                    .photographerCount(2)
                    .shootingStyle("모던")
                    .build();

            assertThat(studio.getInstagramUrl()).isNull();
        }
    }

    // ────────────────────────────────────────────────────────
    // Dress
    // ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Dress 엔티티")
    class DressTests {

        private Dress buildDefaultDress() {
            return Dress.builder()
                    .name("엘레강스 드레스샵")
                    .region("서울")
                    .fullAddress("서울특별시 강남구 청담동 10")
                    .addressDetail("1층")
                    .contactInfo("02-5555-1234")
                    .latitude(37.5200)
                    .longitude(127.0470)
                    .kakaoMapUrl("https://map.kakao.com/link/3")
                    .website("https://elegance-dress.com")
                    .instagramUrl("https://instagram.com/elegance_dress")
                    .description("명품 드레스 전문 브랜드 샵입니다.")
                    .brand("엘레강스")
                    .fittingCount(3)
                    .build();
        }

        @Test
        @DisplayName("빌더로 생성 시 모든 필드가 정상적으로 설정된다")
        void builder_setsAllFields() {
            Dress dress = buildDefaultDress();

            assertThat(dress.getName()).isEqualTo("엘레강스 드레스샵");
            assertThat(dress.getRegion()).isEqualTo("서울");
            assertThat(dress.getFullAddress()).isEqualTo("서울특별시 강남구 청담동 10");
            assertThat(dress.getBrand()).isEqualTo("엘레강스");
            assertThat(dress.getFittingCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("생성 직후 isActive 는 true 이다")
        void isActive_isTrueOnCreation() {
            Dress dress = buildDefaultDress();

            assertThat(dress.isActive()).isTrue();
        }

        @Test
        @DisplayName("getVendorCategory() 는 DRESS 를 반환한다")
        void getVendorCategory_returnsDress() {
            Dress dress = buildDefaultDress();

            assertThat(dress.getVendorCategory()).isEqualTo(VendorCategory.DRESS);
        }

        @Test
        @DisplayName("deactivate() 호출 후 isActive 는 false 가 된다")
        void deactivate_setsIsActiveFalse() {
            Dress dress = buildDefaultDress();

            dress.deactivate();

            assertThat(dress.isActive()).isFalse();
        }

        @Test
        @DisplayName("instagramUrl 이 null 이어도 정상 생성된다 (선택 필드)")
        void instagramUrl_allowsNull() {
            Dress dress = Dress.builder()
                    .name("베이직 드레스")
                    .region("인천")
                    .fullAddress("인천광역시 남동구 구월로 1")
                    .instagramUrl(null)
                    .brand("베이직")
                    .fittingCount(2)
                    .build();

            assertThat(dress.getInstagramUrl()).isNull();
        }
    }

    // ────────────────────────────────────────────────────────
    // Makeup
    // ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Makeup 엔티티")
    class MakeupTests {

        private Makeup buildDefaultMakeup() {
            return Makeup.builder()
                    .name("글로우 메이크업")
                    .region("서울")
                    .fullAddress("서울특별시 서초구 서초대로 20")
                    .addressDetail("4층")
                    .contactInfo("02-7777-8888")
                    .latitude(37.4950)
                    .longitude(127.0280)
                    .kakaoMapUrl("https://map.kakao.com/link/4")
                    .website("https://glow-makeup.com")
                    .instagramUrl("https://instagram.com/glow_makeup")
                    .description("웨딩 메이크업 전문 아티스트 팀입니다.")
                    .artistCount(4)
                    .isHomeCareAvailable(true)
                    .homeCareFee(150000L)
                    .build();
        }

        @Test
        @DisplayName("빌더로 생성 시 모든 필드가 정상적으로 설정된다")
        void builder_setsAllFields() {
            Makeup makeup = buildDefaultMakeup();

            assertThat(makeup.getName()).isEqualTo("글로우 메이크업");
            assertThat(makeup.getRegion()).isEqualTo("서울");
            assertThat(makeup.getFullAddress()).isEqualTo("서울특별시 서초구 서초대로 20");
            assertThat(makeup.getArtistCount()).isEqualTo(4);
            assertThat(makeup.isHomeCareAvailable()).isTrue();
            assertThat(makeup.getHomeCareFee()).isEqualTo(150000L);
        }

        @Test
        @DisplayName("생성 직후 isActive 는 true 이다")
        void isActive_isTrueOnCreation() {
            Makeup makeup = buildDefaultMakeup();

            assertThat(makeup.isActive()).isTrue();
        }

        @Test
        @DisplayName("getVendorCategory() 는 MAKEUP 을 반환한다")
        void getVendorCategory_returnsMakeup() {
            Makeup makeup = buildDefaultMakeup();

            assertThat(makeup.getVendorCategory()).isEqualTo(VendorCategory.MAKEUP);
        }

        @Test
        @DisplayName("deactivate() 호출 후 isActive 는 false 가 된다")
        void deactivate_setsIsActiveFalse() {
            Makeup makeup = buildDefaultMakeup();

            makeup.deactivate();

            assertThat(makeup.isActive()).isFalse();
        }

        @Test
        @DisplayName("instagramUrl 이 null 이어도 정상 생성된다 (선택 필드)")
        void instagramUrl_allowsNull() {
            Makeup makeup = Makeup.builder()
                    .name("내추럴 메이크업")
                    .region("대구")
                    .fullAddress("대구광역시 중구 동성로 1")
                    .instagramUrl(null)
                    .artistCount(2)
                    .isHomeCareAvailable(false)
                    .homeCareFee(null)
                    .build();

            assertThat(makeup.getInstagramUrl()).isNull();
        }

        @Test
        @DisplayName("출장 불가 시 homeCareFee 가 null 이어도 정상 생성된다")
        void homeCareFee_allowsNullWhenHomeCareNotAvailable() {
            Makeup makeup = Makeup.builder()
                    .name("살롱 메이크업")
                    .region("부산")
                    .fullAddress("부산광역시 수영구 수영로 1")
                    .artistCount(3)
                    .isHomeCareAvailable(false)
                    .homeCareFee(null)
                    .build();

            assertThat(makeup.isHomeCareAvailable()).isFalse();
            assertThat(makeup.getHomeCareFee()).isNull();
        }
    }
}
