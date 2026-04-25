package com.wedit.backend.api.vendor.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VendorMedia 엔티티 단위 테스트")
class VendorMediaTest {

    // ────────────────────────────────────────────────────────
    // 테스트용 Vendor 헬퍼 (WeddingHall 을 대표로 사용)
    // ────────────────────────────────────────────────────────

    private WeddingHall buildVendor() {
        return WeddingHall.builder()
                .name("테스트 웨딩홀")
                .region("서울")
                .fullAddress("서울특별시 강남구 테헤란로 1")
                .capacity(300)
                .hallCount(2)
                .mealAvailable(true)
                .parkingAvailable(true)
                .build();
    }

    // ────────────────────────────────────────────────────────
    // 빌더 생성 테스트
    // ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("빌더 생성")
    class BuilderTests {

        @Test
        @DisplayName("빌더로 생성 시 url, ordering, isThumbnail 이 정상적으로 설정된다")
        void builder_setsUrlOrderingAndIsThumbnail() {
            WeddingHall vendor = buildVendor();

            VendorMedia media = VendorMedia.builder()
                    .vendor(vendor)
                    .url("https://cdn.wedit.com/images/hall1.jpg")
                    .ordering(1)
                    .isThumbnail(true)
                    .build();

            assertThat(media.getUrl()).isEqualTo("https://cdn.wedit.com/images/hall1.jpg");
            assertThat(media.getOrdering()).isEqualTo(1);
            assertThat(media.isThumbnail()).isTrue();
            assertThat(media.getVendor()).isSameAs(vendor);
        }

        @Test
        @DisplayName("ordering 에 null 을 전달하면 기본값 0 으로 설정된다")
        void builder_nullOrdering_defaultsToZero() {
            WeddingHall vendor = buildVendor();

            VendorMedia media = VendorMedia.builder()
                    .vendor(vendor)
                    .url("https://cdn.wedit.com/images/hall2.jpg")
                    .ordering(null)
                    .isThumbnail(false)
                    .build();

            assertThat(media.getOrdering()).isEqualTo(0);
        }

        @Test
        @DisplayName("isThumbnail 이 false 이면 false 로 설정된다")
        void builder_isThumbnailFalse_setsCorrectly() {
            WeddingHall vendor = buildVendor();

            VendorMedia media = VendorMedia.builder()
                    .vendor(vendor)
                    .url("https://cdn.wedit.com/images/hall3.jpg")
                    .ordering(2)
                    .isThumbnail(false)
                    .build();

            assertThat(media.isThumbnail()).isFalse();
        }
    }

    // ────────────────────────────────────────────────────────
    // assignVendor() 테스트
    // ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("assignVendor() 메서드")
    class AssignVendorTests {

        @Test
        @DisplayName("assignVendor() 호출 시 vendor 가 정상적으로 변경된다")
        void assignVendor_updatesVendorReference() {
            WeddingHall originalVendor = buildVendor();
            WeddingHall newVendor = WeddingHall.builder()
                    .name("새 웨딩홀")
                    .region("부산")
                    .fullAddress("부산광역시 해운대구 해운대로 99")
                    .capacity(200)
                    .hallCount(1)
                    .mealAvailable(false)
                    .parkingAvailable(true)
                    .build();

            VendorMedia media = VendorMedia.builder()
                    .vendor(originalVendor)
                    .url("https://cdn.wedit.com/images/hall4.jpg")
                    .ordering(0)
                    .isThumbnail(false)
                    .build();

            media.assignVendor(newVendor);

            assertThat(media.getVendor()).isSameAs(newVendor);
            assertThat(media.getVendor().getName()).isEqualTo("새 웨딩홀");
        }

        @Test
        @DisplayName("vendor 없이 생성 후 assignVendor() 로 vendor 를 설정할 수 있다")
        void assignVendor_canSetVendorAfterCreation() {
            WeddingHall vendor = buildVendor();

            VendorMedia media = VendorMedia.builder()
                    .vendor(null)
                    .url("https://cdn.wedit.com/images/hall5.jpg")
                    .ordering(3)
                    .isThumbnail(true)
                    .build();

            media.assignVendor(vendor);

            assertThat(media.getVendor()).isSameAs(vendor);
        }
    }
}
