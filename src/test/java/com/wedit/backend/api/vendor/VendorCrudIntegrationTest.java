package com.wedit.backend.api.vendor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedit.backend.api.vendor.entity.Dress;
import com.wedit.backend.api.vendor.entity.Makeup;
import com.wedit.backend.api.vendor.entity.Studio;
import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.api.vendor.entity.VendorCategory;
import com.wedit.backend.api.vendor.entity.WeddingHall;
import com.wedit.backend.api.vendor.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:wedit-vendor-flow;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Vendor CRUD 통합 테스트")
class VendorCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private VendorRepository vendorRepository;

    @BeforeEach
    void setUp() {
        vendorRepository.deleteAll();
    }

    @Test
    @DisplayName("웨딩홀 업체를 생성할 수 있다")
    void createWeddingHallVendor() throws Exception {
        mockMvc.perform(post("/api/v1/vendors")
                        .with(user("vendor-admin").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "category", "WEDDING_HALL",
                                "name", "라움 웨딩홀",
                                "region", "서울",
                                "fullAddress", "서울시 강남구 웨딩로 1",
                                "contactInfo", "02-1111-2222",
                                "capacity", 320,
                                "hallCount", 2,
                                "mealAvailable", true,
                                "parkingAvailable", true
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("업체 생성 성공"))
                .andExpect(jsonPath("$.data.category").value("WEDDING_HALL"))
                .andExpect(jsonPath("$.data.capacity").value(320))
                .andExpect(jsonPath("$.data.active").value(true));

        Vendor savedVendor = vendorRepository.findAll().getFirst();
        assertThat(savedVendor).isInstanceOf(WeddingHall.class);
        assertThat(savedVendor.getName()).isEqualTo("라움 웨딩홀");
        assertThat(savedVendor.isActive()).isTrue();
    }

    @Test
    @DisplayName("업체 수정 시 업종별 필드와 공통 필드가 함께 갱신된다")
    void updateMakeupVendor() throws Exception {
        Makeup savedVendor = vendorRepository.saveAndFlush(Makeup.builder()
                .name("글로우 메이크업")
                .region("서울")
                .fullAddress("서울시 서초구 메이크업로 1")
                .artistCount(2)
                .isHomeCareAvailable(true)
                .homeCareFee(150_000L)
                .build());

        mockMvc.perform(put("/api/v1/vendors/{vendorId}", savedVendor.getId())
                        .with(user("vendor-admin").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "category", "MAKEUP",
                                "name", "글로우 메이크업 시그니처",
                                "region", "서울",
                                "fullAddress", "서울시 서초구 메이크업로 2",
                                "artistCount", 5,
                                "homeCareAvailable", false
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("업체 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("글로우 메이크업 시그니처"))
                .andExpect(jsonPath("$.data.artistCount").value(5))
                .andExpect(jsonPath("$.data.homeCareAvailable").value(false))
                .andExpect(jsonPath("$.data.homeCareFee").doesNotExist());

        Makeup updatedVendor = (Makeup) vendorRepository.findById(savedVendor.getId()).orElseThrow();
        assertThat(updatedVendor.getName()).isEqualTo("글로우 메이크업 시그니처");
        assertThat(updatedVendor.getArtistCount()).isEqualTo(5);
        assertThat(updatedVendor.isHomeCareAvailable()).isFalse();
        assertThat(updatedVendor.getHomeCareFee()).isNull();
    }

    @Test
    @DisplayName("비활성 업체는 기본 목록과 기본 상세 조회에서 숨겨지고 includeInactive=true로만 조회된다")
    void inactiveVendorIsHiddenByDefaultQueries() throws Exception {
        Studio activeVendor = vendorRepository.saveAndFlush(Studio.builder()
                .name("모먼트 스튜디오")
                .region("서울")
                .fullAddress("서울시 마포구 스튜디오로 1")
                .isOutdoor(true)
                .photographerCount(4)
                .shootingStyle("내추럴")
                .build());
        Dress inactiveVendor = vendorRepository.saveAndFlush(Dress.builder()
                .name("엘르 드레스")
                .region("서울")
                .fullAddress("서울시 강남구 드레스로 2")
                .brand("엘르")
                .fittingCount(3)
                .build());

        mockMvc.perform(delete("/api/v1/vendors/{vendorId}", inactiveVendor.getId())
                        .with(user("vendor-admin").roles("USER")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/vendors")
                        .with(user("vendor-admin").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(activeVendor.getId()))
                .andExpect(jsonPath("$.data[0].category").value("STUDIO"));

        mockMvc.perform(get("/api/v1/vendors")
                        .with(user("vendor-admin").roles("USER"))
                        .param("category", VendorCategory.STUDIO.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("모먼트 스튜디오"));

        mockMvc.perform(get("/api/v1/vendors/{vendorId}", inactiveVendor.getId())
                        .with(user("vendor-admin").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("업체를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/v1/vendors/{vendorId}", inactiveVendor.getId())
                        .with(user("vendor-admin").roles("USER"))
                        .param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(inactiveVendor.getId()))
                .andExpect(jsonPath("$.data.active").value(false))
                .andExpect(jsonPath("$.data.category").value("DRESS"));
    }

    @Test
    @DisplayName("업종 필수 필드가 빠지면 생성에 실패한다")
    void createFailsWhenCategorySpecificFieldMissing() throws Exception {
        mockMvc.perform(post("/api/v1/vendors")
                        .with(user("vendor-admin").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "category", "DRESS",
                                "name", "브랜드 누락 드레스",
                                "region", "서울",
                                "fullAddress", "서울시 강남구 드레스로 4",
                                "fittingCount", 2
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("드레스 브랜드는 필수입니다."));
    }

    @Test
    @DisplayName("기본 입력 검증에 실패하면 400을 반환한다")
    void createFailsWhenBeanValidationFails() throws Exception {
        mockMvc.perform(post("/api/v1/vendors")
                        .with(user("vendor-admin").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "category", "WEDDING_HALL",
                                "name", "",
                                "region", "서울",
                                "fullAddress", "",
                                "capacity", 200,
                                "hallCount", 1,
                                "mealAvailable", true,
                                "parkingAvailable", true
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("업체 수정 시 카테고리를 바꿀 수 없다")
    void updateFailsWhenCategoryChanges() throws Exception {
        WeddingHall savedVendor = vendorRepository.saveAndFlush(WeddingHall.builder()
                .name("카테고리 유지 홀")
                .region("서울")
                .fullAddress("서울시 강남구 홀로 1")
                .capacity(250)
                .hallCount(2)
                .mealAvailable(true)
                .parkingAvailable(true)
                .build());

        mockMvc.perform(put("/api/v1/vendors/{vendorId}", savedVendor.getId())
                        .with(user("vendor-admin").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "category", "STUDIO",
                                "name", "카테고리 변경 시도",
                                "region", "서울",
                                "fullAddress", "서울시 강남구 홀로 1",
                                "outdoor", true,
                                "photographerCount", 3,
                                "shootingStyle", "화보형"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("업체 카테고리는 변경할 수 없습니다."));
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
