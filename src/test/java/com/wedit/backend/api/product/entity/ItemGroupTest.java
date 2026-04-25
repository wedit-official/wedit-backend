package com.wedit.backend.api.product.entity;

import com.wedit.backend.api.vendor.entity.WeddingHall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ItemGroup 엔티티 단위 테스트")
class ItemGroupTest {

    private WeddingHall vendor;

    @BeforeEach
    void setUp() {
        vendor = WeddingHall.builder()
                .name("테스트홀")
                .region("서울")
                .fullAddress("서울시 강남구 테스트로 1")
                .capacity(300)
                .hallCount(2)
                .mealAvailable(true)
                .parkingAvailable(true)
                .build();
    }

    @Nested
    @DisplayName("빌더 생성")
    class Builder {

        @Test
        @DisplayName("빌더로 생성 시 필드가 정상적으로 설정된다")
        void builderSetsFields() {
            ItemGroup itemGroup = ItemGroup.builder()
                    .vendor(vendor)
                    .name("기본 패키지")
                    .description("기본 패키지 설명입니다")
                    .build();

            assertThat(itemGroup.getVendor()).isEqualTo(vendor);
            assertThat(itemGroup.getName()).isEqualTo("기본 패키지");
            assertThat(itemGroup.getDescription()).isEqualTo("기본 패키지 설명입니다");
        }

        @Test
        @DisplayName("cachedMinPrice 초기값은 0이다")
        void cachedMinPriceDefaultIsZero() {
            ItemGroup itemGroup = ItemGroup.builder()
                    .vendor(vendor)
                    .name("패키지")
                    .description("설명")
                    .build();

            assertThat(itemGroup.getCachedMinPrice()).isEqualTo(0L);
        }

        @Test
        @DisplayName("isDeleted 초기값은 false이다")
        void isDeletedDefaultIsFalse() {
            ItemGroup itemGroup = ItemGroup.builder()
                    .vendor(vendor)
                    .name("패키지")
                    .description("설명")
                    .build();

            assertThat(itemGroup.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("products는 빈 리스트로 초기화된다")
        void productsDefaultIsEmptyList() {
            ItemGroup itemGroup = ItemGroup.builder()
                    .vendor(vendor)
                    .name("패키지")
                    .description("설명")
                    .build();

            assertThat(itemGroup.getProducts()).isNotNull();
            assertThat(itemGroup.getProducts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateCachedMinPrice()")
    class UpdateCachedMinPrice {

        @Test
        @DisplayName("updateCachedMinPrice() 호출 시 cachedMinPrice가 업데이트된다")
        void updatesCachedMinPrice() {
            ItemGroup itemGroup = ItemGroup.builder()
                    .vendor(vendor)
                    .name("패키지")
                    .description("설명")
                    .build();

            itemGroup.updateCachedMinPrice(500_000L);

            assertThat(itemGroup.getCachedMinPrice()).isEqualTo(500_000L);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("delete() 호출 후 isDeleted가 true가 된다")
        void deleteSetsIsDeletedTrue() {
            ItemGroup itemGroup = ItemGroup.builder()
                    .vendor(vendor)
                    .name("패키지")
                    .description("설명")
                    .build();

            itemGroup.delete();

            assertThat(itemGroup.isDeleted()).isTrue();
        }
    }
}
