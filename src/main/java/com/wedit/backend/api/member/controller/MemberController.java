package com.wedit.backend.api.member.controller;

import com.wedit.backend.api.member.dto.MemberLoginRequestDTO;
import com.wedit.backend.api.member.dto.MemberLoginResponseDTO;
import com.wedit.backend.api.member.dto.MemberSignupRequestDTO;
import com.wedit.backend.api.member.service.MemberService;
import com.wedit.backend.common.config.security.entity.SecurityMember;
import com.wedit.backend.common.response.ApiResponse;
import com.wedit.backend.common.response.ErrorStatus;
import com.wedit.backend.common.response.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody MemberSignupRequestDTO dto) {
        try {
            memberService.signup(dto);
            return ApiResponse.successOnly(SuccessStatus.MEMBER_SIGNUP_SUCCESS);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(ErrorStatus.CONFLICT_DUPLICATE_RESOURCE.getStatusCode())
                    .body(ApiResponse.fail(ErrorStatus.CONFLICT_DUPLICATE_RESOURCE.getStatusCode(), ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody MemberLoginRequestDTO dto) {
        try {
            MemberLoginResponseDTO response = memberService.login(dto);
            return ResponseEntity
                    .status(SuccessStatus.FORM_LOGIN_SUCCESS.getStatusCode())
                    .header("Authorization", "Bearer " + response.getAccessToken())
                    .header("X-Refresh-Token", "Bearer " + response.getRefreshToken())
                    .body(ApiResponse.success(SuccessStatus.FORM_LOGIN_SUCCESS, response).getBody());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(ErrorStatus.UNAUTHORIZED_USER.getStatusCode())
                    .body(ApiResponse.fail(ErrorStatus.UNAUTHORIZED_USER.getStatusCode(), ex.getMessage()));
        }
    }

    @PostMapping("/token-reissue")
    public ResponseEntity<ApiResponse<?>> reissueToken(
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshHeader) {
        try {
            String refreshToken = extractBearer(refreshHeader)
                    .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰이 필요합니다."));

            MemberLoginResponseDTO response = memberService.reissueTokens(refreshToken);
            return ResponseEntity
                    .status(SuccessStatus.TOKEN_REISSUE_SUCCESS.getStatusCode())
                    .header("Authorization", "Bearer " + response.getAccessToken())
                    .header("X-Refresh-Token", "Bearer " + response.getRefreshToken())
                    .body(ApiResponse.success(SuccessStatus.TOKEN_REISSUE_SUCCESS, response).getBody());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(ErrorStatus.UNAUTHORIZED_USER.getStatusCode())
                    .body(ApiResponse.fail(ErrorStatus.UNAUTHORIZED_USER.getStatusCode(), ex.getMessage()));
        }
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityMember securityMember)) {
            return ResponseEntity.status(ErrorStatus.UNAUTHORIZED_USER.getStatusCode())
                    .body(ApiResponse.fail(ErrorStatus.UNAUTHORIZED_USER.getStatusCode(), "인증이 필요합니다."));
        }

        memberService.withdraw(securityMember.getMember().getId());
        return ApiResponse.successOnly(SuccessStatus.MEMBER_WITHDRAW_SUCCESS);
    }

    private Optional<String> extractBearer(String headerValue) {
        if (!StringUtils.hasText(headerValue)) {
            return Optional.empty();
        }
        if (headerValue.startsWith("Bearer ")) {
            return Optional.of(headerValue.substring(7));
        }
        return Optional.of(headerValue);
    }
}
