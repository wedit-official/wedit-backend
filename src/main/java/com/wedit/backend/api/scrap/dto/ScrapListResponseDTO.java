package com.wedit.backend.api.scrap.dto;

import com.wedit.backend.api.vendor.entity.VendorCategory;

public record ScrapListResponseDTO(
        Long vendorId,
        String vendorName,
        VendorCategory category,
        String region,
        String thumbnailUrl,
        long basePrice
) {
    public static ScrapListResponseDTO of(
            Long vendorId,
            String vendorName,
            VendorCategory category,
            String region,
            String thumbnailUrl,
            Long cachedMinPrice
    ) {
        return new ScrapListResponseDTO(
                vendorId,
                vendorName,
                category,
                region,
                thumbnailUrl,
                cachedMinPrice != null ? cachedMinPrice : 0L
        );
    }
}
