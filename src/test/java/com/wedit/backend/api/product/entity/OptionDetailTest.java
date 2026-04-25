package com.wedit.backend.api.product.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OptionDetail 엔티티 단위 테스트")
class OptionDetailTest {

    @Nested
    @DisplayName("빌더 생성")
    class Builder {

        @Test
        @DisplayName("빌더 생성 시 필드가 정상적으로 설정된다")
        void builderSetsFields() {
            OptionDetail detail = OptionDetail.builder()
                    .name("2시간")
                    .price(100_000L)
                    .unit("시간")
                    .maxCount(3)
                    .ordering(1)
                    .build();

            assertThat(detail.getName()).isEqualTo("2시간");
            assertThat(detail.getPrice()).isEqualTo(100_000L);
            assertThat(detail.getUnit()).isEqualTo("시간");
            assertThat(detail.getMaxCount()).isEqualTo(3);
            assertThat(detail.getOrdering()).isEqualTo(1);
        }

        @Test
        @DisplayName("price가 null이면 0L이 기본값으로 설정된다")
        void priceNullDefaultsToZero() {
            OptionDetail detail = OptionDetail.builder()
                    .name("기본 포함")
                    .price(null)
                    .build();

            assertThat(detail.getPrice()).isEqualTo(0L);
        }

        @Test
        @DisplayName("ordering이 null이면 0이 기본값으로 설정된다")
        void orderingNullDefaultsToZero() {
            OptionDetail detail = OptionDetail.builder()
                    .name("옵션")
                    .ordering(null)
                    .build();

            assertThat(detail.getOrdering()).isEqualTo(0);
        }

        @Test
        @DisplayName("생성 직후 isSoldOut은 false이다")
        void isSoldOutDefaultIsFalse() {
            OptionDetail detail = OptionDetail.builder()
                    .name("옵션")
                    .build();

            assertThat(detail.isSoldOut()).isFalse();
        }
    }

    @Nested
    @DisplayName("toggleSoldOut()")
    class ToggleSoldOut {

        @Test
        @DisplayName("toggleSoldOut() 첫 번째 호출 시 isSoldOut이 true가 된다")
        void firstToggleSetsTrue() {
            OptionDetail detail = OptionDetail.builder()
                    .name("옵션")
                    .build();

            detail.toggleSoldOut();

            assertThat(detail.isSoldOut()).isTrue();
        }

        @Test
        @DisplayName("toggleSoldOut() 두 번째 호출 시 isSoldOut이 다시 false가 된다")
        void secondToggleSetsFalse() {
            OptionDetail detail = OptionDetail.builder()
                    .name("옵션")
                    .build();

            detail.toggleSoldOut();
            detail.toggleSoldOut();

            assertThat(detail.isSoldOut()).isFalse();
        }
    }

    @Nested
    @DisplayName("isQuantityBased()")
    class IsQuantityBased {

        @Test
        @DisplayName("unit이 null이면 isQuantityBased()는 false를 반환한다")
        void unitNullReturnsFalse() {
            OptionDetail detail = OptionDetail.builder()
                    .name("A라인 드레스")
                    .unit(null)
                    .build();

            assertThat(detail.isQuantityBased()).isFalse();
        }

        @Test
        @DisplayName("unit이 있으면 isQuantityBased()는 true를 반환한다")
        void unitPresentReturnsTrue() {
            OptionDetail detail = OptionDetail.builder()
                    .name("원본 파일")
                    .unit("장")
                    .build();

            assertThat(detail.isQuantityBased()).isTrue();
        }
    }
}
