package com.wedit.backend.api.product.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OptionGroup 엔티티 단위 테스트")
class OptionGroupTest {

    @Nested
    @DisplayName("빌더 생성 - 기본값")
    class BuilderDefaults {

        @Test
        @DisplayName("필수 옵션(isMandatory=true) 생성 시 minSelectCount 기본값은 1이다")
        void mandatoryOptionDefaultMinSelectCountIsOne() {
            OptionGroup optionGroup = OptionGroup.builder()
                    .name("촬영 시간")
                    .isMandatory(true)
                    .build();

            assertThat(optionGroup.getMinSelectCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("추가 옵션(isMandatory=false) 생성 시 minSelectCount 기본값은 0이다")
        void additionalOptionDefaultMinSelectCountIsZero() {
            OptionGroup optionGroup = OptionGroup.builder()
                    .name("포토북")
                    .isMandatory(false)
                    .build();

            assertThat(optionGroup.getMinSelectCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("maxSelectCount가 null이면 기본값 1이 설정된다")
        void maxSelectCountNullDefaultsToOne() {
            OptionGroup optionGroup = OptionGroup.builder()
                    .name("드레스 스타일")
                    .isMandatory(true)
                    .maxSelectCount(null)
                    .build();

            assertThat(optionGroup.getMaxSelectCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("ordering이 null이면 기본값 0이 설정된다")
        void orderingNullDefaultsToZero() {
            OptionGroup optionGroup = OptionGroup.builder()
                    .name("드레스 스타일")
                    .isMandatory(false)
                    .ordering(null)
                    .build();

            assertThat(optionGroup.getOrdering()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("addOptionDetail()")
    class AddOptionDetail {

        @Test
        @DisplayName("addOptionDetail() 호출 시 optionDetails에 추가되고 양방향 참조가 설정된다")
        void addOptionDetailUpdatesBothSides() {
            OptionGroup optionGroup = OptionGroup.builder()
                    .name("촬영 시간")
                    .isMandatory(true)
                    .build();

            OptionDetail detail = OptionDetail.builder()
                    .name("2시간")
                    .price(100_000L)
                    .build();

            optionGroup.addOptionDetail(detail);

            assertThat(optionGroup.getOptionDetails()).hasSize(1);
            assertThat(optionGroup.getOptionDetails()).contains(detail);
            assertThat(detail.getOptionGroup()).isEqualTo(optionGroup);
        }
    }
}
