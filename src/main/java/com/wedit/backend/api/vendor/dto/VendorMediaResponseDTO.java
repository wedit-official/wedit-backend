package com.wedit.backend.api.vendor.dto;

import com.wedit.backend.api.vendor.entity.VendorMedia;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VendorMediaResponseDTO {

    private final Long id;
    private final String url;
    private final Integer ordering;
    private final boolean thumbnail;

    public static VendorMediaResponseDTO from(VendorMedia media) {
        return VendorMediaResponseDTO.builder()
                .id(media.getId())
                .url(media.getUrl())
                .ordering(media.getOrdering())
                .thumbnail(media.isThumbnail())
                .build();
    }
}
