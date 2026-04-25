package com.wedit.backend.api.product.repository;

import com.wedit.backend.api.product.entity.ItemGroup;
import com.wedit.backend.api.product.entity.OptionDetail;
import com.wedit.backend.api.product.entity.OptionGroup;
import com.wedit.backend.api.product.entity.Product;
import com.wedit.backend.api.vendor.entity.WeddingHall;
import com.wedit.backend.support.JpaTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JpaTestConfig.JpaIntegrationTest
@DisplayName("Product JPA 통합 테스트")
class ProductJpaTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OptionGroupRepository optionGroupRepository;

    @Autowired
    OptionDetailRepository optionDetailRepository;

    private ItemGroup savedItemGroup;

    @BeforeEach
    void setUp() {
        WeddingHall vendor = WeddingHall.builder()
                .name("테스트 웨딩홀")
                .region("서울")
                .fullAddress("서울시 강남구 테스트로 1")
                .capacity(300)
                .hallCount(2)
                .mealAvailable(true)
                .parkingAvailable(true)
                .build();
        em.persistAndFlush(vendor);

        ItemGroup itemGroup = ItemGroup.builder()
                .vendor(vendor)
                .name("기본 패키지 그룹")
                .description("테스트용 아이템 그룹")
                .build();
        savedItemGroup = em.persistAndFlush(itemGroup);
    }

    private Product createBasicProduct(String name) {
        return Product.builder()
                .itemGroup(savedItemGroup)
                .name(name)
                .basePrice(500_000L)
                .build();
    }

    @Nested
    @DisplayName("OptionGroup cascade 저장")
    class OptionGroupCascade {

        @Test
        @DisplayName("Product 저장 후 OptionGroup 추가 시 cascade ALL로 OptionGroup도 저장된다")
        void addOptionGroupCascadeSaves() {
            Product product = createBasicProduct("스탠다드 패키지");
            OptionGroup optionGroup = OptionGroup.builder()
                    .name("촬영 시간")
                    .isMandatory(true)
                    .minSelectCount(1)
                    .maxSelectCount(1)
                    .ordering(1)
                    .build();
            product.addOptionGroup(optionGroup);
            em.persistAndFlush(product);
            em.clear();

            Product found = productRepository.findById(product.getId()).orElseThrow();
            assertThat(found.getOptionGroups()).hasSize(1);
            assertThat(found.getOptionGroups().get(0).getName()).isEqualTo("촬영 시간");
        }

        @Test
        @DisplayName("OptionGroup에 OptionDetail 추가 시 cascade ALL로 OptionDetail도 저장된다")
        void addOptionDetailCascadeSaves() {
            Product product = createBasicProduct("디럭스 패키지");
            OptionGroup optionGroup = OptionGroup.builder()
                    .name("드레스 스타일")
                    .isMandatory(true)
                    .build();
            OptionDetail detail = OptionDetail.builder()
                    .name("A라인")
                    .price(50_000L)
                    .ordering(1)
                    .build();
            optionGroup.addOptionDetail(detail);
            product.addOptionGroup(optionGroup);
            em.persistAndFlush(product);
            em.clear();

            Product found = productRepository.findById(product.getId()).orElseThrow();
            OptionGroup foundGroup = found.getOptionGroups().get(0);
            assertThat(foundGroup.getOptionDetails()).hasSize(1);
            assertThat(foundGroup.getOptionDetails().get(0).getName()).isEqualTo("A라인");
            assertThat(foundGroup.getOptionDetails().get(0).getPrice()).isEqualTo(50_000L);
        }
    }

    @Nested
    @DisplayName("orphanRemoval 삭제")
    class OrphanRemoval {

        @Test
        @DisplayName("Product 삭제 시 orphanRemoval로 OptionGroup과 OptionDetail도 함께 삭제된다")
        void deleteProductRemovesOptionGroupsAndDetails() {
            Product product = createBasicProduct("삭제 테스트 패키지");
            OptionGroup optionGroup = OptionGroup.builder()
                    .name("포토북")
                    .isMandatory(false)
                    .build();
            OptionDetail detail = OptionDetail.builder()
                    .name("20페이지")
                    .price(80_000L)
                    .ordering(1)
                    .build();
            optionGroup.addOptionDetail(detail);
            product.addOptionGroup(optionGroup);
            em.persistAndFlush(product);

            Long productId = product.getId();
            Long optionGroupId = optionGroup.getId();
            Long optionDetailId = detail.getId();
            em.clear();

            productRepository.deleteById(productId);
            productRepository.flush();
            em.clear();

            assertThat(productRepository.findById(productId)).isEmpty();
            assertThat(optionGroupRepository.findById(optionGroupId)).isEmpty();
            assertThat(optionDetailRepository.findById(optionDetailId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Product 상태 변경")
    class ProductStatus {

        @Test
        @DisplayName("publish() 후 저장 시 isVisible=true로 조회된다")
        void publishPersistsIsVisibleTrue() {
            Product product = createBasicProduct("공개 테스트 패키지");
            em.persistAndFlush(product);
            Long id = product.getId();

            Product managed = productRepository.findById(id).orElseThrow();
            managed.publish();
            productRepository.saveAndFlush(managed);
            em.clear();

            Product found = productRepository.findById(id).orElseThrow();
            assertThat(found.isVisible()).isTrue();
        }
    }

    @Nested
    @DisplayName("OptionGroup minSelectCount 저장")
    class OptionGroupMinSelectCount {

        @Test
        @DisplayName("isMandatory=true인 OptionGroup의 minSelectCount가 정상 저장된다")
        void mandatoryOptionGroupMinSelectCountPersists() {
            Product product = createBasicProduct("필수 옵션 패키지");
            OptionGroup mandatoryGroup = OptionGroup.builder()
                    .name("기본 구성")
                    .isMandatory(true)
                    .minSelectCount(1)
                    .maxSelectCount(1)
                    .ordering(1)
                    .build();
            product.addOptionGroup(mandatoryGroup);
            em.persistAndFlush(product);
            em.clear();

            Product found = productRepository.findById(product.getId()).orElseThrow();
            Optional<OptionGroup> foundGroup = found.getOptionGroups().stream()
                    .filter(OptionGroup::isMandatory)
                    .findFirst();

            assertThat(foundGroup).isPresent();
            assertThat(foundGroup.get().isMandatory()).isTrue();
            assertThat(foundGroup.get().getMinSelectCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("isMandatory=false인 OptionGroup의 minSelectCount가 정상 저장된다")
        void additionalOptionGroupMinSelectCountPersists() {
            Product product = createBasicProduct("추가 옵션 패키지");
            OptionGroup additionalGroup = OptionGroup.builder()
                    .name("추가 구성")
                    .isMandatory(false)
                    .minSelectCount(0)
                    .maxSelectCount(3)
                    .ordering(2)
                    .build();
            product.addOptionGroup(additionalGroup);
            em.persistAndFlush(product);
            em.clear();

            Product found = productRepository.findById(product.getId()).orElseThrow();
            Optional<OptionGroup> foundGroup = found.getOptionGroups().stream()
                    .filter(g -> !g.isMandatory())
                    .findFirst();

            assertThat(foundGroup).isPresent();
            assertThat(foundGroup.get().isMandatory()).isFalse();
            assertThat(foundGroup.get().getMinSelectCount()).isEqualTo(0);
        }
    }
}
