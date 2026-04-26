package com.wedit.backend.api.vendor.controller;

import com.wedit.backend.api.vendor.dto.VendorResponseDTO;
import com.wedit.backend.api.vendor.dto.VendorUpsertRequestDTO;
import com.wedit.backend.api.vendor.entity.VendorCategory;
import com.wedit.backend.api.vendor.service.VendorService;
import com.wedit.backend.common.exception.NotFoundException;
import com.wedit.backend.common.response.ApiResponse;
import com.wedit.backend.common.response.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vendors")
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    public ResponseEntity<ApiResponse<VendorResponseDTO>> createVendor(
            @Valid @RequestBody VendorUpsertRequestDTO request
    ) {
        return ApiResponse.success(SuccessStatus.VENDOR_CREATE_SUCCESS, vendorService.createVendor(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorResponseDTO>>> getVendors(
            @RequestParam(required = false) VendorCategory category,
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ApiResponse.success(
                SuccessStatus.VENDOR_LIST_SUCCESS,
                vendorService.getVendors(category, includeInactive)
        );
    }

    @GetMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<VendorResponseDTO>> getVendor(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ApiResponse.success(
                SuccessStatus.VENDOR_DETAIL_SUCCESS,
                vendorService.getVendor(vendorId, includeInactive)
        );
    }

    @PutMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<VendorResponseDTO>> updateVendor(
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorUpsertRequestDTO request
    ) {
        return ApiResponse.success(
                SuccessStatus.VENDOR_UPDATE_SUCCESS,
                vendorService.updateVendor(vendorId, request)
        );
    }

    @DeleteMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable Long vendorId) {
        vendorService.deleteVendor(vendorId);
        return ApiResponse.successOnly(SuccessStatus.VENDOR_DELETE_SUCCESS);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(ApiResponse.fail(exception.getStatusCode(), exception.getResponseMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
    }
}
