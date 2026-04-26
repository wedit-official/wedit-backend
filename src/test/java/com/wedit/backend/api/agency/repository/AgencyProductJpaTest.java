package com.wedit.backend.api.agency.repository;

import com.wedit.backend.api.agency.entity.Agency;
import com.wedit.backend.api.agency.entity.AgencyProduct;
import com.wedit.backend.api.product.entity.ItemGroup;
import com.wedit.backend.api.product.entity.Product;
import com.wedit.backend.api.product.repository.ItemGroupRepository;
import com.wedit.backend.api.product.repository.ProductRepository;
import com.wedit.backend.api.vendor.entity.WeddingHall;
import com.wedit.backend.api.vendor.repository.WeddingHallRepository;
import com.wedit.backend.support.JpaTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JpaTestConfig.JpaIntegrationTest
@DisplayName("AgencyProduct JPA 통합 테스트")
class AgencyProductJpaTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    AgencyRepository agencyRepository;

    @Autowired
    AgencyProductRepository agencyProductRepository;

    @Autowired
    WeddingHallRepository weddingHallRepository;

    @Autowired
    ItemGroupRepository itemGroupRepository;

    @Autowired
    ProductRepository productRepository;

    private WeddingHall savedVendor;
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
        savedVendor = em.persistAndFlush(vendor);

        ItemGroup itemGroup = ItemGroup.builder()
                .vendor(savedVendor)
                .name("기본 패키지 그룹")
                .description("테스트용 아이템 그룹")
                .build();
        savedItemGroup = em.persistAndFlush(itemGroup);
    }

    private Product createAndSaveProduct(String name) {
        Product product = Product.builder()
                .itemGroup(savedItemGroup)
                .name(name)
                .basePrice(500_000L)
                .build();
        return em.persistAndFlush(product);
    }

    private Agency createAndSaveAgency(String name) {
        Agency agency = Agency.builder()
                .name(name)
                .phone("02-1234-5678")
                .website("https://agency.example.com")
                .build();
        return em.persistAndFlush(agency);
    }

    @Nested
    @DisplayName("AgencyProduct 기본 저장 및 조회")
    class BasicSaveAndFind {

        @Test
        @DisplayName("Agency + Product + AgencyProduct 저장 후 정상 조회된다")
        void saveAndFindAgencyProduct() {
            Agency agency = createAndSaveAgency("테스트 대행사");
            Product product = createAndSaveProduct("스탠다드 패키지");

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(450_000L)
                    .build();
            em.persistAndFlush(agencyProduct);
            em.clear();

            Optional<AgencyProduct> found = agencyProductRepository.findById(agencyProduct.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getAgencyPrice()).isEqualTo(450_000L);
            assertThat(found.get().isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("unique constraint")
    class UniqueConstraint {

        @Test
        @DisplayName("같은 (agency, product) 쌍으로 AgencyProduct 중복 저장 시 예외가 발생한다")
        void duplicateAgencyProductThrowsException() {
            Agency agency = createAndSaveAgency("유니크 테스트 대행사");
            Product product = createAndSaveProduct("유니크 테스트 패키지");

            AgencyProduct first = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(400_000L)
                    .build();
            em.persistAndFlush(first);

            AgencyProduct duplicate = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(420_000L)
                    .build();

            assertThatThrownBy(() -> {
                em.persistAndFlush(duplicate);
            }).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("AgencyProduct 상태 변경")
    class StatusChange {

        @Test
        @DisplayName("updatePrice() 후 저장 시 DB에 새 가격이 반영된다")
        void updatePricePersistsNewPrice() {
            Agency agency = createAndSaveAgency("가격 변경 대행사");
            Product product = createAndSaveProduct("가격 변경 패키지");

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(300_000L)
                    .build();
            em.persistAndFlush(agencyProduct);
            Long id = agencyProduct.getId();

            AgencyProduct managed = agencyProductRepository.findById(id).orElseThrow();
            managed.updatePrice(350_000L);
            agencyProductRepository.saveAndFlush(managed);
            em.clear();

            AgencyProduct found = agencyProductRepository.findById(id).orElseThrow();
            assertThat(found.getAgencyPrice()).isEqualTo(350_000L);
        }

        @Test
        @DisplayName("deactivate() 후 저장 시 isActive=false가 DB에 반영된다")
        void deactivatePersistsIsActiveFalse() {
            Agency agency = createAndSaveAgency("비활성 대행사");
            Product product = createAndSaveProduct("비활성 패키지");

            AgencyProduct agencyProduct = AgencyProduct.builder()
                    .agency(agency)
                    .product(product)
                    .agencyPrice(200_000L)
                    .build();
            em.persistAndFlush(agencyProduct);
            Long id = agencyProduct.getId();

            AgencyProduct managed = agencyProductRepository.findById(id).orElseThrow();
            managed.deactivate();
            agencyProductRepository.saveAndFlush(managed);
            em.clear();

            AgencyProduct found = agencyProductRepository.findById(id).orElseThrow();
            assertThat(found.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("1:N 관계 조회")
    class OneToManyRelation {

        @Test
        @DisplayName("Agency 1개에 Product 2개 연결 시 agencyProducts 리스트 size가 2이다")
        void oneAgencyTwoProductsListSizeIsTwo() {
            Agency agency = createAndSaveAgency("멀티 상품 대행사");
            Product product1 = createAndSaveProduct("패키지 A");
            Product product2 = createAndSaveProduct("패키지 B");

            AgencyProduct ap1 = AgencyProduct.builder()
                    .agency(agency)
                    .product(product1)
                    .agencyPrice(100_000L)
                    .build();
            AgencyProduct ap2 = AgencyProduct.builder()
                    .agency(agency)
                    .product(product2)
                    .agencyPrice(200_000L)
                    .build();
            em.persistAndFlush(ap1);
            em.persistAndFlush(ap2);
            em.clear();

            Agency found = agencyRepository.findById(agency.getId()).orElseThrow();
            assertThat(found.getAgencyProducts()).hasSize(2);
        }

        @Test
        @DisplayName("Product 1개를 Agency 2개가 판매 시 서로 다른 agencyPrice 설정이 가능하다")
        void twoAgenciesSameProductDifferentPrices() {
            Agency agency1 = createAndSaveAgency("대행사 알파");
            Agency agency2 = createAndSaveAgency("대행사 베타");
            Product product = createAndSaveProduct("공유 패키지");

            AgencyProduct ap1 = AgencyProduct.builder()
                    .agency(agency1)
                    .product(product)
                    .agencyPrice(480_000L)
                    .build();
            AgencyProduct ap2 = AgencyProduct.builder()
                    .agency(agency2)
                    .product(product)
                    .agencyPrice(520_000L)
                    .build();
            em.persistAndFlush(ap1);
            em.persistAndFlush(ap2);
            em.clear();

            AgencyProduct foundAp1 = agencyProductRepository.findById(ap1.getId()).orElseThrow();
            AgencyProduct foundAp2 = agencyProductRepository.findById(ap2.getId()).orElseThrow();

            assertThat(foundAp1.getAgencyPrice()).isEqualTo(480_000L);
            assertThat(foundAp2.getAgencyPrice()).isEqualTo(520_000L);
        }
    }
}
