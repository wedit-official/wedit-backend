package com.wedit.backend.api.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.entity.Role;
import com.wedit.backend.api.member.jwt.entity.RefreshToken;
import com.wedit.backend.api.member.jwt.repository.RefreshTokenRepository;
import com.wedit.backend.api.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:wedit-member-flow;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Member/Auth 통합 테스트")
class MemberFlowIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signupSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "user@wedit.com",
                                "password", "Password123!",
                                "name", "웨딧 유저",
                                "birthDate", "1994-05-20",
                                "phoneNumber", "010-1234-5678",
                                "spouseType", "BRIDE"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));

        Member savedMember = memberRepository.findByEmailAndDeletedFalse("user@wedit.com").orElseThrow();
        assertThat(savedMember.getName()).isEqualTo("웨딧 유저");
        assertThat(passwordEncoder.matches("Password123!", savedMember.getPassword())).isTrue();
    }

    @Test
    @DisplayName("중복 이메일 회원가입 실패")
    void signupDuplicateEmailFails() throws Exception {
        saveMember("duplicate@wedit.com", "Password123!", "중복 사용자");

        mockMvc.perform(post("/api/v1/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "duplicate@wedit.com",
                                "password", "Password123!",
                                "name", "다른 이름",
                                "birthDate", "1994-05-20",
                                "phoneNumber", "010-1234-5678",
                                "spouseType", "BRIDE"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }

    @Test
    @DisplayName("회원가입 입력 검증 실패")
    void signupValidationFails() throws Exception {
        mockMvc.perform(post("/api/v1/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "not-an-email",
                                "password", "short",
                                "name", ""
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() throws Exception {
        saveMember("login@wedit.com", "Password123!", "로그인 사용자");

        mockMvc.perform(post("/api/v1/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "login@wedit.com",
                                "password", "Password123!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andExpect(header().exists("X-Refresh-Token"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString());

        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 로그인 실패")
    void loginFailsWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/v1/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "missing@wedit.com",
                                "password", "Password123!"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    @Test
    @DisplayName("비밀번호 불일치 로그인 실패")
    void loginFailsWhenPasswordDoesNotMatch() throws Exception {
        saveMember("wrong-password@wedit.com", "Password123!", "비밀번호 사용자");

        mockMvc.perform(post("/api/v1/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "wrong-password@wedit.com",
                                "password", "WrongPassword123!"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void tokenReissueSuccess() throws Exception {
        Member member = saveMember("reissue@wedit.com", "Password123!", "재발급 사용자");
        String refreshToken = issueRefreshTokenViaLogin("reissue@wedit.com", "Password123!");

        MvcResult result = mockMvc.perform(post("/api/v1/member/token-reissue")
                        .header("X-Refresh-Token", bearer(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andExpect(header().exists("X-Refresh-Token"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String reissuedRefreshToken = tokenFromHeader(result.getResponse().getHeader("X-Refresh-Token"));
        List<RefreshToken> savedTokens = refreshTokenRepository.findAllByMemberId(member.getId());

        assertThat(savedTokens).hasSize(1);
        assertThat(savedTokens.get(0).getToken()).isEqualTo(reissuedRefreshToken);
        assertThat(reissuedRefreshToken).isNotEqualTo(refreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰 헤더가 없으면 재발급 실패")
    void tokenReissueFailsWhenHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/v1/member/token-reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 필요합니다."));
    }

    @Test
    @DisplayName("잘못된 리프레시 토큰이면 재발급 실패")
    void tokenReissueFailsWhenTokenInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/member/token-reissue")
                        .header("X-Refresh-Token", bearer("invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."));
    }

    @Test
    @DisplayName("인증 사용자는 회원 탈퇴할 수 있다")
    void withdrawSuccess() throws Exception {
        Member member = saveMember("withdraw@wedit.com", "Password123!", "탈퇴 사용자");
        String accessToken = issueAccessTokenViaLogin("withdraw@wedit.com", "Password123!");

        mockMvc.perform(delete("/api/v1/member/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNoContent());

        Member deletedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(deletedMember.isDeleted()).isTrue();
        assertThat(refreshTokenRepository.findAllByMemberId(member.getId())).isEmpty();
    }

    @Test
    @DisplayName("미인증 사용자는 회원 탈퇴할 수 없다")
    void withdrawFailsWhenUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/member/withdraw"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증되지 않은 사용자입니다."));
    }

    private Member saveMember(String email, String rawPassword, String name) {
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .name(name)
                .role(Role.ROLE_USER)
                .build();
        return memberRepository.save(member);
    }

    private String issueAccessTokenViaLogin(String email, String password) throws Exception {
        MvcResult result = login(email, password);
        return tokenFromHeader(result.getResponse().getHeader(HttpHeaders.AUTHORIZATION));
    }

    private String issueRefreshTokenViaLogin(String email, String password) throws Exception {
        MvcResult result = login(email, password);
        return tokenFromHeader(result.getResponse().getHeader("X-Refresh-Token"));
    }

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String tokenFromHeader(String headerValue) {
        assertThat(headerValue).isNotBlank();
        return headerValue.startsWith("Bearer ") ? headerValue.substring(7) : headerValue;
    }
}
