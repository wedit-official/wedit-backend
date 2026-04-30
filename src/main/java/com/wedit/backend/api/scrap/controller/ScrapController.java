package com.wedit.backend.api.scrap.controller;

import com.wedit.backend.api.scrap.dto.ScrapToggleResponseDTO;
import com.wedit.backend.api.scrap.service.ScrapService;
import com.wedit.backend.common.config.security.entity.SecurityMember;
import com.wedit.backend.common.exception.NotFoundException;
import com.wedit.backend.common.response.ApiResponse;
import com.wedit.backend.common.response.ErrorStatus;
import com.wedit.backend.common.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vendors/{vendorId}/scrap")
public class ScrapController {

    private final ScrapService scrapService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScrapToggleResponseDTO>> toggle(
            @PathVariable Long vendorId
    ) {
        SecurityMember securityMember = resolveSecurityMember();
        if (securityMember == null) {
            return unauthorized();
        }

        ScrapToggleResponseDTO resp = scrapService.toggle(
                securityMember.getMember().getId(), vendorId
        );

        return ApiResponse.success(SuccessStatus.SCRAP_TOGGLE_SUCCESS, resp);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ScrapToggleResponseDTO>> getStatus(
            @PathVariable Long vendorId
    ) {
        SecurityMember securityMember = resolveSecurityMember();
        if (securityMember == null) {
            return unauthorized();
        }

        ScrapToggleResponseDTO resp = scrapService.getStatus(
                securityMember.getMember().getId(), vendorId
        );

        return ApiResponse.success(SuccessStatus.SCRAP_STATUS_SUCCESS, resp);
    }

    private SecurityMember resolveSecurityMember() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityMember sm)) {
            return null;
        }
        return sm;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(ApiResponse.fail(exception.getStatusCode(), exception.getResponseMessage()));
    }

    private ResponseEntity<ApiResponse<?>> unauthorized() {
        return ResponseEntity.status(ErrorStatus.UNAUTHORIZED_USER.getStatusCode())
                .body(ApiResponse.fail(
                        ErrorStatus.UNAUTHORIZED_USER.getStatusCode(),
                        ErrorStatus.UNAUTHORIZED_USER.getMessage()
                ));
    }
}
