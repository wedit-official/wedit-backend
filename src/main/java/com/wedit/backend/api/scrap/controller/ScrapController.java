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
    public ResponseEntity<?> toggle(
            @PathVariable Long vendorId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityMember securityMember)) {
            return ResponseEntity.status(ErrorStatus.UNAUTHORIZED_USER.getStatusCode())
                    .body(ApiResponse.fail(ErrorStatus.UNAUTHORIZED_USER.getStatusCode(), "인증이 필요합니다."));
        }

        ScrapToggleResponseDTO resp = scrapService.toggle(securityMember.getMember().getId(), vendorId);

        return ApiResponse.success(SuccessStatus.SCRAP_TOGGLE_SUCCESS, resp);
    }

    @GetMapping
    public ResponseEntity<?> getStatus(
            @PathVariable Long vendorId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityMember securityMember)) {
            return ResponseEntity.status(ErrorStatus.UNAUTHORIZED_USER.getStatusCode())
                    .body(ApiResponse.fail(ErrorStatus.UNAUTHORIZED_USER.getStatusCode(), "인증이 필요합니다."));
        }

        ScrapToggleResponseDTO resp = scrapService.getStatus(securityMember.getMember().getId(), vendorId);

        return ApiResponse.success(SuccessStatus.SCRAP_STATUS_SUCCESS, resp);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            NotFoundException exception
    ) {
        ApiResponse<Void> body = ApiResponse.fail(exception.getStatusCode(), exception.getResponseMessage());

        return ResponseEntity.status(exception.getStatusCode()).body(body);
    }
}
