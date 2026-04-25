package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.*;
import com.wedit.backend.support.JpaTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JpaTestConfig.JpaIntegrationTest
@DisplayName("Vendor JPA 통합 테스트")
class VendorJpaTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    WeddingHallRepository weddingHallRepository;

    @Autowired
    StudioRepository studioRepository;

    @Autowired
    DressRepository dressRepository;

    @Autowired
    MakeupRepository makeupRepository;

    private WeddingHall buildWeddingHall(String name) {
        return WeddingHall.builder()
                .name(name)
                .region("서울")
                .fullAddress("서울시 강남구 테스트로 1")
                .capacity(300)
                .hallCount(2)
                .mealAvailable(true)
                .parkingAvailable(true)
                .build();
    }

    @Nested
    @DisplayName("JOINED 상속 전략 조회")
    class InheritanceQuery {

        @Test
        @DisplayName("WeddingHall 저장 후 VendorRepository로 조회 시 WeddingHall 인스턴스로 반환된다")
        void findByVendorRepositoryReturnsWeddingHallInstance() {
            WeddingHall hall = buildWeddingHall("그랜드 웨딩홀");
            em.persistAndFlush(hall);
            em.clear();

            Optional<Vendor> found = vendorRepository.findById(hall.getId());

            assertThat(found).isPresent();
            assertThat(found.get()).isInstanceOf(WeddingHall.class);
        }

        @Test
        @DisplayName("WeddingHall 저장 후 WeddingHallRepository로 조회 시 고유 필드가 정상 반환된다")
        void findByWeddingHallRepositoryReturnsUniqueFields() {
            WeddingHall hall = buildWeddingHall("로얄 웨딩홀");
            em.persistAndFlush(hall);
            em.clear();

            Optional<WeddingHall> found = weddingHallRepository.findById(hall.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getCapacity()).isEqualTo(300);
            assertThat(found.get().getHallCount()).isEqualTo(2);
            assertThat(found.get().isMealAvailable()).isTrue();
            assertThat(found.get().isParkingAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("각 Vendor 서브타입 저장 및 카테고리 반환")
    class VendorSubtypes {

        @Test
        @DisplayName("Studio 저장 후 조회 시 getVendorCategory()가 STUDIO를 반환한다")
        void studioReturnsCorrectCategory() {
            Studio studio = Studio.builder()
                    .name("모던 스튜디오")
                    .region("서울")
                    .fullAddress("서울시 마포구 스튜디오로 5")
                    .isOutdoor(true)
                    .photographerCount(3)
                    .shootingStyle("자연광 웨딩")
                    .build();
            em.persistAndFlush(studio);
            em.clear();

            Optional<Studio> found = studioRepository.findById(studio.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getVendorCategory()).isEqualTo(VendorCategory.STUDIO);
            assertThat(found.get().isOutdoor()).isTrue();
            assertThat(found.get().getPhotographerCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Dress 저장 후 조회 시 getVendorCategory()가 DRESS를 반환한다")
        void dressReturnsCorrectCategory() {
            Dress dress = Dress.builder()
                    .name("엘레강스 드레스")
                    .region("부산")
                    .fullAddress("부산시 해운대구 드레스로 10")
                    .brand("엘리자베스")
                    .fittingCount(2)
                    .build();
            em.persistAndFlush(dress);
            em.clear();

            Optional<Dress> found = dressRepository.findById(dress.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getVendorCategory()).isEqualTo(VendorCategory.DRESS);
            assertThat(found.get().getBrand()).isEqualTo("엘리자베스");
            assertThat(found.get().getFittingCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Makeup 저장 후 조회 시 getVendorCategory()가 MAKEUP을 반환한다")
        void makeupReturnsCorrectCategory() {
            Makeup makeup = Makeup.builder()
                    .name("뷰티 메이크업")
                    .region("서울")
                    .fullAddress("서울시 송파구 메이크업로 3")
                    .artistCount(5)
                    .isHomeCareAvailable(true)
                    .homeCareFee(50_000L)
                    .build();
            em.persistAndFlush(makeup);
            em.clear();

            Optional<Makeup> found = makeupRepository.findById(makeup.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getVendorCategory()).isEqualTo(VendorCategory.MAKEUP);
            assertThat(found.get().getArtistCount()).isEqualTo(5);
            assertThat(found.get().isHomeCareAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("Vendor 상태 변경")
    class VendorStatus {

        @Test
        @DisplayName("deactivate() 후 저장하면 isActive=false로 DB에 반영된다")
        void deactivatePersistsIsActiveFalse() {
            WeddingHall hall = buildWeddingHall("비활성 테스트홀");
            em.persistAndFlush(hall);
            Long id = hall.getId();

            WeddingHall managed = weddingHallRepository.findById(id).orElseThrow();
            managed.deactivate();
            weddingHallRepository.saveAndFlush(managed);
            em.clear();

            Optional<WeddingHall> found = weddingHallRepository.findById(id);

            assertThat(found).isPresent();
            assertThat(found.get().isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("VendorMedia cascade")
    class VendorMediaCascade {

        @Test
        @DisplayName("WeddingHall에 미디어 추가 후 저장하면 vendor_media 테이블에 함께 저장된다")
        void addMediaCascadeSavesToVendorMediaTable() {
            WeddingHall hall = buildWeddingHall("미디어 테스트홀");
            VendorMedia media = VendorMedia.builder()
                    .url("https://cdn.example.com/image1.jpg")
                    .ordering(1)
                    .isThumbnail(true)
                    .build();
            hall.addMedia(media);

            em.persistAndFlush(hall);
            em.clear();

            WeddingHall found = weddingHallRepository.findById(hall.getId()).orElseThrow();
            assertThat(found.getMediaList()).hasSize(1);
            assertThat(found.getMediaList().get(0).getUrl()).isEqualTo("https://cdn.example.com/image1.jpg");
            assertThat(found.getMediaList().get(0).isThumbnail()).isTrue();
        }
    }

    @Nested
    @DisplayName("nullable 필드 저장")
    class NullableFields {

        @Test
        @DisplayName("instagramUrl이 null인 Vendor를 저장하고 조회할 수 있다")
        void instagramUrlNullSaveAndLoad() {
            WeddingHall hall = WeddingHall.builder()
                    .name("인스타 없는 홀")
                    .region("대전")
                    .fullAddress("대전시 유성구 테스트로 7")
                    .instagramUrl(null)
                    .capacity(150)
                    .hallCount(1)
                    .mealAvailable(false)
                    .parkingAvailable(true)
                    .build();

            em.persistAndFlush(hall);
            em.clear();

            WeddingHall found = weddingHallRepository.findById(hall.getId()).orElseThrow();

            assertThat(found.getInstagramUrl()).isNull();
        }
    }
}
