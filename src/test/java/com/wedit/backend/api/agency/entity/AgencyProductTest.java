package com.wedit.backend.api.agency.entity;

import com.wedit.backend.api.product.entity.ItemGroup;
import com.wedit.backend.api.product.entity.Product;
import com.wedit.backend.api.vendor.entity.WeddingHall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AgencyProduct 엔티티 단위 테스트")
class AgencyProductTest {

    // ────────────────────────────────────────────────────────
    // 테스트용 픽스처 헬퍼
    // ────────────────────────────────────────────────────────

    private Agency buildAgency() {
        return Agency.builder()
                .name("테스트 대행사")
                .phone("02-1234-5678")
                .website("https://test-agency.com")
                .build();
    }

    private WeddingHall buildWeddingHall() {
        return WeddingHall.builder()
                .name("테스트 웨딩홀")
                .region("서울")
                .fullAddress("서울특별시 강남구 테헤란로 1")
                .capacity(500)
                .hallCount(3)
                .mealAvailable(true)
                .parkingAvailable(true)
                .build();
    }

    private ItemGroup buildItemGroup(WeddingHall vendor) {
        return ItemGroup.builder()
                .vendor(vendor)
                .name("기본 패키지")
                .description("웨딩홀 기본 패키지 상품 그룹")
                .build();
    }

    private Product buildProduct(ItemGroup itemGroup) {
        return Product.builder()
                .itemGroup(itemGroup)
                .name("웨딩 패키지 A")
                .basePrice(3000000L)
                .build();
    }

    // ────────────────────────────────────────────────────────
    // 빌더 생성 테스트
    // ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("빌더 생성")
    class BuilderTests {

        @Test
        @DisplayName("빌더로 생성 시 agency, product, agencyPrice 가 정상적으로 설정된다")
        void builder_setsAgencyProductAndAgencyPrice() {
            Agency agency = buildAgency();
            WeddingHall vendor = buildWeddingHall();
            ItemGroup itemGroup = buildItemGroup(vendor);
            Product product = buildProduct(itemGroup);

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(2500000L)
                    .build();

            assertThat(agencyProduct.getAgency()).isSameAs(agency);
            assertThat(agencyProduct.getProduct()).isSameAs(product);
            assertThat(agencyProduct.getAgencyPrice()).isEqualTo(2500000L);
        }

        @Test
        @DisplayName("생성 직후 isActive 는 true 이다")
        void isActive_isTrueOnCreation() {
            Agency agency = buildAgency();
            WeddingHall vendor = buildWeddingHall();
            ItemGroup itemGroup = buildItemGroup(vendor);
            Product product = buildProduct(itemGroup);

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(1000000L)
                    .build();

            assertThat(agencyProduct.isActive()).isTrue();
        }

        @Test
        @DisplayName("agencyPrice 가 0 이어도 정상 생성된다")
        void builder_zeroPriceIsValid() {
            Agency agency = buildAgency();
            WeddingHall vendor = buildWeddingHall();
            ItemGroup itemGroup = buildItemGroup(vendor);
            Product product = buildProduct(itemGroup);

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(0L)
                    .build();

            assertThat(agencyProduct.getAgencyPrice()).isEqualTo(0L);
        }
    }

    // ────────────────────────────────────────────────────────
    // updatePrice() 테스트
    // ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updatePrice() 메서드")
    class UpdatePriceTests {

        @Test
        @DisplayName("updatePrice() 호출 시 agencyPrice 가 새 값으로 변경된다")
        void updatePrice_changesAgencyPrice() {
            Agency agency = buildAgency();
            WeddingHall vendor = buildWeddingHall();
            ItemGroup itemGroup = buildItemGroup(vendor);
            Product product = buildProduct(itemGroup);

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(2000000L)
                    .build();

            agencyProduct.updatePrice(2500000L);

            assertThat(agencyProduct.getAgencyPrice()).isEqualTo(2500000L);
        }

        @Test
        @DisplayName("updatePrice() 는 isActive 에 영향을 주지 않는다")
        void updatePrice_doesNotAffectIsActive() {
            Agency agency = buildAgency();
            WeddingHall vendor = buildWeddingHall();
            ItemGroup itemGroup = buildItemGroup(vendor);
            Product product = buildProduct(itemGroup);

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(2000000L)
                    .build();

            agencyProduct.updatePrice(3000000L);

            assertThat(agencyProduct.isActive()).isTrue();
        }

        @Test
        @DisplayName("updatePrice() 를 여러 번 호출하면 마지막 값으로 최종 설정된다")
        void updatePrice_multipleCallsUsesLastValue() {
            Agency agency = buildAgency();
            WeddingHall vendor = buildWeddingHall();
            ItemGroup itemGroup = buildItemGroup(vendor);
            Product product = buildProduct(itemGroup);

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(1000000L)
                    .build();

            agencyProduct.updatePrice(2000000L);
            agencyProduct.updatePrice(3000000L);

            assertThat(agencyProduct.getAgencyPrice()).isEqualTo(3000000L);
        }
    }

    // ────────────────────────────────────────────────────────
    // deactivate() 테스트
    // ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deactivate() 메서드")
    class DeactivateTests {

        @Test
        @DisplayName("deactivate() 호출 후 isActive 는 false 가 된다")
        void deactivate_setsIsActiveFalse() {
            Agency agency = buildAgency();
            WeddingHall vendor = buildWeddingHall();
            ItemGroup itemGroup = buildItemGroup(vendor);
            Product product = buildProduct(itemGroup);

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(1500000L)
                    .build();

            agencyProduct.deactivate();

            assertThat(agencyProduct.isActive()).isFalse();
        }

        @Test
        @DisplayName("deactivate() 는 agencyPrice 에 영향을 주지 않는다")
        void deactivate_doesNotAffectAgencyPrice() {
            Agency agency = buildAgency();
            WeddingHall vendor = buildWeddingHall();
            ItemGroup itemGroup = buildItemGroup(vendor);
            Product product = buildProduct(itemGroup);

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(1800000L)
                    .build();

            agencyProduct.deactivate();

            assertThat(agencyProduct.getAgencyPrice()).isEqualTo(1800000L);
        }

        @Test
        @DisplayName("deactivate() 는 agency, product 참조에 영향을 주지 않는다")
        void deactivate_doesNotAffectAssociations() {
            Agency agency = buildAgency();
            WeddingHall vendor = buildWeddingHall();
            ItemGroup itemGroup = buildItemGroup(vendor);
            Product product = buildProduct(itemGroup);

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(2200000L)
                    .build();

            agencyProduct.deactivate();

            assertThat(agencyProduct.getAgency()).isSameAs(agency);
            assertThat(agencyProduct.getProduct()).isSameAs(product);
        }
    }
}
