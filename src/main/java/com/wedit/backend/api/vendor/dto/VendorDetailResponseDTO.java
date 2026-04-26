package com.wedit.backend.api.vendor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.api.vendor.entity.VendorCategory;
import com.wedit.backend.api.vendor.entity.VendorMedia;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VendorDetailResponseDTO {

    private final Long id;
    private final VendorCategory category;
    private final String name;
    private final String region;
    private final String fullAddress;
    private final String addressDetail;
    private final String contactInfo;
    private final Double latitude;
    private final Double longitude;
    private final String kakaoMapUrl;
    private final String website;
    private final String instagramUrl;
    private final String description;
    private final boolean active;
    private final VendorSubtypeResponseDTO details;
    private final VendorMediaResponseDTO mainMedia;
    private final List<VendorMediaResponseDTO> mediaList;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static VendorDetailResponseDTO from(Vendor vendor) {
        List<VendorMediaResponseDTO> mediaResponses = sortedMedia(vendor).stream()
                .map(VendorMediaResponseDTO::from)
                .toList();

        return VendorDetailResponseDTO.builder()
                .id(vendor.getId())
                .category(vendor.getVendorCategory())
                .name(vendor.getName())
                .region(vendor.getRegion())
                .fullAddress(vendor.getFullAddress())
                .addressDetail(vendor.getAddressDetail())
                .contactInfo(vendor.getContactInfo())
                .latitude(vendor.getLatitude())
                .longitude(vendor.getLongitude())
                .kakaoMapUrl(vendor.getKakaoMapUrl())
                .website(vendor.getWebsite())
                .instagramUrl(vendor.getInstagramUrl())
                .description(vendor.getDescription())
                .active(vendor.isActive())
                .details(VendorSubtypeResponseDTO.from(vendor))
                .mainMedia(resolveMainMedia(mediaResponses))
                .mediaList(mediaResponses)
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();
    }

    private static List<VendorMedia> sortedMedia(Vendor vendor) {
        return vendor.getMediaList().stream()
                .sorted(Comparator.comparing(VendorMedia::getOrdering)
                        .thenComparing(media -> media.getId() == null ? Long.MAX_VALUE : media.getId()))
                .toList();
    }

    private static VendorMediaResponseDTO resolveMainMedia(List<VendorMediaResponseDTO> mediaList) {
        return mediaList.stream()
                .filter(VendorMediaResponseDTO::isThumbnail)
                .findFirst()
                .orElseGet(() -> mediaList.isEmpty() ? null : mediaList.getFirst());
    }
}
