package com.wedit.backend.api.product.entity;

import com.wedit.backend.api.vendor.entity.WeddingHall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product 엔티티 단위 테스트")
class ProductTest {

    private ItemGroup itemGroup;

    @BeforeEach
    void setUp() {
        WeddingHall vendor = WeddingHall.builder()
                .name("홀")
                .region("서울")
                .fullAddress("주소")
                .capacity(200)
                .hallCount(1)
                .mealAvailable(true)
                .parkingAvailable(true)
                .build();

        itemGroup = ItemGroup.builder()
                .vendor(vendor)
                .name("그룹")
                .description("설명")
                .build();
    }

    @Nested
    @DisplayName("빌더 생성")
    class Builder {

        @Test
        @DisplayName("빌더 생성 시 필드가 정상적으로 설정된다")
        void builderSetsFields() {
            Product product = Product.builder()
                    .itemGroup(itemGroup)
                    .name("스탠다드 패키지")
                    .basePrice(300_000L)
                    .tags(List.of("웨딩", "스튜디오"))
                    .build();

            assertThat(product.getItemGroup()).isEqualTo(itemGroup);
            assertThat(product.getName()).isEqualTo("스탠다드 패키지");
            assertThat(product.getBasePrice()).isEqualTo(300_000L);
            assertThat(product.getTags()).containsExactly("웨딩", "스튜디오");
        }

        @Test
        @DisplayName("basePrice가 null이면 0L이 기본값으로 설정된다")
        void basePriceNullDefaultsToZero() {
            Product product = Product.builder()
                    .itemGroup(itemGroup)
                    .name("패키지")
                    .basePrice(null)
                    .build();

            assertThat(product.getBasePrice()).isEqualTo(0L);
        }

        @Test
        @DisplayName("tags가 null이면 빈 리스트가 기본값으로 설정된다")
        void tagsNullDefaultsToEmptyList() {
            Product product = Product.builder()
                    .itemGroup(itemGroup)
                    .name("패키지")
                    .tags(null)
                    .build();

            assertThat(product.getTags()).isNotNull();
            assertThat(product.getTags()).isEmpty();
        }

        @Test
        @DisplayName("생성 직후 isVisible=false, isDeleted=false이다")
        void defaultVisibilityAndDeletedAreFalse() {
            Product product = Product.builder()
                    .itemGroup(itemGroup)
                    .name("패키지")
                    .build();

            assertThat(product.isVisible()).isFalse();
            assertThat(product.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("publish() / hide()")
    class PublishAndHide {

        @Test
        @DisplayName("publish() 호출 시 isVisible이 true가 된다")
        void publishSetsVisibleTrue() {
            Product product = Product.builder()
                    .itemGroup(itemGroup)
                    .name("패키지")
                    .build();

            product.publish();

            assertThat(product.isVisible()).isTrue();
        }

        @Test
        @DisplayName("hide() 호출 시 isVisible이 false가 된다")
        void hideSetsVisibleFalse() {
            Product product = Product.builder()
                    .itemGroup(itemGroup)
                    .name("패키지")
                    .build();

            product.publish();
            product.hide();

            assertThat(product.isVisible()).isFalse();
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("delete() 호출 후 isDeleted=true, isVisible=false이다")
        void deleteSetsDeletedTrueAndVisibleFalse() {
            Product product = Product.builder()
                    .itemGroup(itemGroup)
                    .name("패키지")
                    .build();

            product.publish();
            product.delete();

            assertThat(product.isDeleted()).isTrue();
            assertThat(product.isVisible()).isFalse();
        }
    }

    @Nested
    @DisplayName("addOptionGroup()")
    class AddOptionGroup {

        @Test
        @DisplayName("addOptionGroup() 호출 시 optionGroups에 추가되고 양방향 참조가 설정된다")
        void addOptionGroupUpdatesBothSides() {
            Product product = Product.builder()
                    .itemGroup(itemGroup)
                    .name("패키지")
                    .build();

            OptionGroup optionGroup = OptionGroup.builder()
                    .name("촬영 시간")
                    .isMandatory(true)
                    .build();

            product.addOptionGroup(optionGroup);

            assertThat(product.getOptionGroups()).hasSize(1);
            assertThat(product.getOptionGroups()).contains(optionGroup);
            assertThat(optionGroup.getProduct()).isEqualTo(product);
        }
    }
}
