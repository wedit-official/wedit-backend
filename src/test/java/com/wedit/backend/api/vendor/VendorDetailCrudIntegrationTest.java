package com.wedit.backend.api.vendor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedit.backend.api.vendor.entity.Dress;
import com.wedit.backend.api.vendor.entity.Studio;
import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.api.vendor.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:wedit-vendor-detail;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Vendor detail CRUD 통합 테스트")
class VendorDetailCrudIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VendorRepository vendorRepository;

    @BeforeEach
    void setUp() {
        vendorRepository.deleteAll();
    }

    @Test
    @DisplayName("업체 상세를 생성, 조회, 수정, 삭제할 수 있다")
    void createReadUpdateDeleteVendorDetail() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/vendors")
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(weddingHallPayload("라움 웨딩홀"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("업체 생성 성공"))
                .andExpect(jsonPath("$.data.category").value("WEDDING_HALL"))
                .andExpect(jsonPath("$.data.details.capacity").value(320))
                .andExpect(jsonPath("$.data.mainMedia.url").value("https://cdn.wedit.com/hall-main.jpg"))
                .andExpect(jsonPath("$.data.mediaList.length()").value(2))
                .andReturn();

        Long vendorId = responseDataId(createResult);

        mockMvc.perform(get("/api/v1/vendors/{vendorId}", vendorId)
                        .with(vendorAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("업체 상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(vendorId))
                .andExpect(jsonPath("$.data.fullAddress").value("서울시 강남구 웨딩로 1"))
                .andExpect(jsonPath("$.data.latitude").value(37.501))
                .andExpect(jsonPath("$.data.longitude").value(127.039))
                .andExpect(jsonPath("$.data.kakaoMapUrl").value("https://map.kakao.com/link/raum"))
                .andExpect(jsonPath("$.data.mediaList[0].url").value("https://cdn.wedit.com/hall-main.jpg"));

        mockMvc.perform(put("/api/v1/vendors/{vendorId}", vendorId)
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updatedWeddingHallPayload())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("업체 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("라움 웨딩홀 리뉴얼"))
                .andExpect(jsonPath("$.data.details.capacity").value(420))
                .andExpect(jsonPath("$.data.mainMedia.url").value("https://cdn.wedit.com/hall-renewal.jpg"))
                .andExpect(jsonPath("$.data.mediaList.length()").value(1));

        Vendor updatedVendor = vendorRepository.findDetailById(vendorId).orElseThrow();
        assertThat(updatedVendor.getName()).isEqualTo("라움 웨딩홀 리뉴얼");
        assertThat(updatedVendor.getMediaList()).hasSize(1);

        mockMvc.perform(delete("/api/v1/vendors/{vendorId}", vendorId)
                        .with(vendorAdmin()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/vendors/{vendorId}", vendorId)
                        .with(vendorAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("업체를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/v1/vendors/{vendorId}", vendorId)
                        .with(vendorAdmin())
                        .param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    @DisplayName("업체 목록은 카테고리와 지역으로 필터링하고 기본적으로 비활성 업체를 숨긴다")
    void listFiltersByCategoryAndRegionAndHidesInactiveVendors() throws Exception {
        Studio studio = vendorRepository.saveAndFlush(Studio.builder()
                .name("모먼트 스튜디오")
                .region("서울")
                .fullAddress("서울시 마포구 스튜디오로 1")
                .isOutdoor(true)
                .photographerCount(4)
                .shootingStyle("내추럴")
                .build());
        vendorRepository.saveAndFlush(Dress.builder()
                .name("엘르 드레스")
                .region("부산")
                .fullAddress("부산시 해운대구 드레스로 2")
                .brand("엘르")
                .fittingCount(3)
                .build());
        Dress inactiveDress = vendorRepository.saveAndFlush(Dress.builder()
                .name("숨겨진 드레스")
                .region("서울")
                .fullAddress("서울시 강남구 드레스로 3")
                .brand("히든")
                .fittingCount(1)
                .build());
        inactiveDress.deactivate();
        vendorRepository.saveAndFlush(inactiveDress);

        assertThat(vendorRepository.findActiveDetails())
                .extracting(Vendor::getId)
                .contains(studio.getId())
                .doesNotContain(inactiveDress.getId());

        mockMvc.perform(get("/api/v1/vendors")
                        .with(vendorAdmin())
                        .param("category", "STUDIO")
                        .param("region", "서울"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("업체 목록 조회 성공"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(studio.getId()))
                .andExpect(jsonPath("$.data[0].category").value("STUDIO"));

        mockMvc.perform(get("/api/v1/vendors")
                        .with(vendorAdmin())
                        .param("category", "DRESS")
                        .param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("스튜디오, 드레스, 메이크업 업체 상세를 생성할 수 있다")
    void createSupportsEveryVendorCategory() throws Exception {
        mockMvc.perform(post("/api/v1/vendors")
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(studioPayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.category").value("STUDIO"))
                .andExpect(jsonPath("$.data.details.outdoor").value(true))
                .andExpect(jsonPath("$.data.details.shootingStyle").value("화보형"));

        mockMvc.perform(post("/api/v1/vendors")
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dressPayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.category").value("DRESS"))
                .andExpect(jsonPath("$.data.details.brand").value("엘르"))
                .andExpect(jsonPath("$.data.mainMedia.url").value("https://cdn.wedit.com/dress.jpg"));

        mockMvc.perform(post("/api/v1/vendors")
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(makeupPayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.category").value("MAKEUP"))
                .andExpect(jsonPath("$.data.details.artistCount").value(5))
                .andExpect(jsonPath("$.data.details.homeCareFee").value(150000));
    }

    @Test
    @DisplayName("기본 입력 검증에 실패하면 400을 반환한다")
    void createFailsWhenBeanValidationFails() throws Exception {
        Map<String, Object> payload = weddingHallPayload("");
        payload.put("fullAddress", "");

        mockMvc.perform(post("/api/v1/vendors")
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("대표 이미지를 둘 이상 지정하면 생성에 실패한다")
    void createFailsWhenMoreThanOneThumbnailExists() throws Exception {
        Map<String, Object> payload = weddingHallPayload("대표 이미지 중복 홀");
        payload.put("mediaList", List.of(
                media("https://cdn.wedit.com/a.jpg", 1, true),
                media("https://cdn.wedit.com/b.jpg", 2, true)
        ));

        mockMvc.perform(post("/api/v1/vendors")
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("대표 이미지는 하나만 지정할 수 있습니다."));
    }

    @Test
    @DisplayName("카테고리별 필수 필드가 빠지면 생성에 실패한다")
    void createFailsWhenCategorySpecificFieldMissing() throws Exception {
        Map<String, Object> payload = dressPayload();
        payload.remove("brand");

        mockMvc.perform(post("/api/v1/vendors")
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("드레스 브랜드는 필수입니다."));
    }

    @Test
    @DisplayName("업체 수정 시 카테고리를 바꿀 수 없다")
    void updateFailsWhenCategoryChanges() throws Exception {
        Long vendorId = responseDataId(mockMvc.perform(post("/api/v1/vendors")
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(weddingHallPayload("카테고리 유지 홀"))))
                .andExpect(status().isCreated())
                .andReturn());

        mockMvc.perform(put("/api/v1/vendors/{vendorId}", vendorId)
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(studioPayload())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("업체 카테고리는 변경할 수 없습니다."));
    }

    @Test
    @DisplayName("위도와 경도는 함께 입력해야 한다")
    void createFailsWhenOnlyOneCoordinateIsProvided() throws Exception {
        Map<String, Object> payload = weddingHallPayload("좌표 누락 홀");
        payload.remove("longitude");

        mockMvc.perform(post("/api/v1/vendors")
                        .with(vendorAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("위도와 경도는 함께 입력해야 합니다."));
    }

    private Map<String, Object> weddingHallPayload(String name) {
        Map<String, Object> payload = basePayload("WEDDING_HALL", name);
        payload.put("capacity", 320);
        payload.put("hallCount", 2);
        payload.put("mealAvailable", true);
        payload.put("parkingAvailable", true);
        payload.put("mediaList", List.of(
                media("https://cdn.wedit.com/hall-sub.jpg", 2, false),
                media("https://cdn.wedit.com/hall-main.jpg", 1, true)
        ));
        return payload;
    }

    private Map<String, Object> updatedWeddingHallPayload() {
        Map<String, Object> payload = weddingHallPayload("라움 웨딩홀 리뉴얼");
        payload.put("capacity", 420);
        payload.put("fullAddress", "서울시 강남구 리뉴얼로 7");
        payload.put("mediaList", List.of(media("https://cdn.wedit.com/hall-renewal.jpg", 0, false)));
        return payload;
    }

    private Map<String, Object> studioPayload() {
        Map<String, Object> payload = basePayload("STUDIO", "무드 스튜디오");
        payload.put("outdoor", true);
        payload.put("photographerCount", 3);
        payload.put("shootingStyle", "화보형");
        return payload;
    }

    private Map<String, Object> dressPayload() {
        Map<String, Object> payload = basePayload("DRESS", "엘르 드레스");
        payload.put("brand", "엘르");
        payload.put("fittingCount", 2);
        payload.put("mediaList", List.of(media("https://cdn.wedit.com/dress.jpg", 0, false)));
        return payload;
    }

    private Map<String, Object> makeupPayload() {
        Map<String, Object> payload = basePayload("MAKEUP", "글로우 메이크업");
        payload.put("artistCount", 5);
        payload.put("homeCareAvailable", true);
        payload.put("homeCareFee", 150_000L);
        return payload;
    }

    private Map<String, Object> basePayload(String category, String name) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("category", category);
        payload.put("name", name);
        payload.put("region", "서울");
        payload.put("fullAddress", "서울시 강남구 웨딩로 1");
        payload.put("addressDetail", "3층");
        payload.put("contactInfo", "02-1111-2222");
        payload.put("latitude", 37.501);
        payload.put("longitude", 127.039);
        payload.put("kakaoMapUrl", "https://map.kakao.com/link/raum");
        payload.put("website", "https://vendor.wedit.com");
        payload.put("instagramUrl", "https://instagram.com/wedit_vendor");
        payload.put("description", "업체 상세 소개입니다.");
        return payload;
    }

    private Map<String, Object> media(String url, int ordering, boolean thumbnail) {
        return Map.of(
                "url", url,
                "ordering", ordering,
                "thumbnail", thumbnail
        );
    }

    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor vendorAdmin() {
        return SecurityMockMvcRequestPostProcessors.user("vendor-admin").roles("USER");
    }

    private Long responseDataId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
