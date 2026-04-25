package com.wedit.backend.api.agency.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Agency 엔티티 단위 테스트")
class AgencyTest {

    // ────────────────────────────────────────────────────────
    // 빌더 생성 테스트
    // ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("빌더 생성")
    class BuilderTests {

        @Test
        @DisplayName("빌더로 생성 시 name, phone, website 가 정상적으로 설정된다")
        void builder_setsNamePhoneAndWebsite() {
            Agency agency = Agency.builder()
                    .name("웨딩 대행사 A")
                    .phone("02-1111-2222")
                    .website("https://agency-a.com")
                    .build();

            assertThat(agency.getName()).isEqualTo("웨딩 대행사 A");
            assertThat(agency.getPhone()).isEqualTo("02-1111-2222");
            assertThat(agency.getWebsite()).isEqualTo("https://agency-a.com");
        }

        @Test
        @DisplayName("생성 직후 isActive 는 true 이다")
        void isActive_isTrueOnCreation() {
            Agency agency = Agency.builder()
                    .name("웨딩 대행사 B")
                    .phone("02-3333-4444")
                    .website("https://agency-b.com")
                    .build();

            assertThat(agency.isActive()).isTrue();
        }

        @Test
        @DisplayName("생성 직후 agencyProducts 는 빈 리스트이다")
        void agencyProducts_isEmptyOnCreation() {
            Agency agency = Agency.builder()
                    .name("웨딩 대행사 C")
                    .phone("02-5555-6666")
                    .website("https://agency-c.com")
                    .build();

            assertThat(agency.getAgencyProducts()).isNotNull();
            assertThat(agency.getAgencyProducts()).isEmpty();
        }

        @Test
        @DisplayName("phone 이 null 이어도 정상 생성된다 (선택 필드)")
        void phone_allowsNull() {
            Agency agency = Agency.builder()
                    .name("웨딩 대행사 D")
                    .phone(null)
                    .website("https://agency-d.com")
                    .build();

            assertThat(agency.getPhone()).isNull();
            assertThat(agency.getName()).isEqualTo("웨딩 대행사 D");
        }

        @Test
        @DisplayName("website 가 null 이어도 정상 생성된다 (선택 필드)")
        void website_allowsNull() {
            Agency agency = Agency.builder()
                    .name("웨딩 대행사 E")
                    .phone("02-7777-8888")
                    .website(null)
                    .build();

            assertThat(agency.getWebsite()).isNull();
            assertThat(agency.getName()).isEqualTo("웨딩 대행사 E");
        }

        @Test
        @DisplayName("phone 과 website 가 모두 null 이어도 정상 생성된다")
        void phoneAndWebsite_bothAllowNull() {
            Agency agency = Agency.builder()
                    .name("웨딩 대행사 F")
                    .phone(null)
                    .website(null)
                    .build();

            assertThat(agency.getPhone()).isNull();
            assertThat(agency.getWebsite()).isNull();
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
            Agency agency = Agency.builder()
                    .name("비활성 대행사")
                    .phone("02-0000-0000")
                    .website("https://inactive.com")
                    .build();

            agency.deactivate();

            assertThat(agency.isActive()).isFalse();
        }

        @Test
        @DisplayName("deactivate() 는 name, phone, website 에 영향을 주지 않는다")
        void deactivate_doesNotAffectOtherFields() {
            Agency agency = Agency.builder()
                    .name("불변 대행사")
                    .phone("02-1234-5678")
                    .website("https://stable.com")
                    .build();

            agency.deactivate();

            assertThat(agency.getName()).isEqualTo("불변 대행사");
            assertThat(agency.getPhone()).isEqualTo("02-1234-5678");
            assertThat(agency.getWebsite()).isEqualTo("https://stable.com");
        }
    }
}
