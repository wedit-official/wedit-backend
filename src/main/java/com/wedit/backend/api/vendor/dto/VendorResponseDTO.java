package com.wedit.backend.api.vendor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wedit.backend.api.vendor.entity.Dress;
import com.wedit.backend.api.vendor.entity.Makeup;
import com.wedit.backend.api.vendor.entity.Studio;
import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.api.vendor.entity.VendorCategory;
import com.wedit.backend.api.vendor.entity.WeddingHall;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VendorResponseDTO {

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
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Integer capacity;
    private final Integer hallCount;
    private final Boolean mealAvailable;
    private final Boolean parkingAvailable;
    private final Boolean outdoor;
    private final Integer photographerCount;
    private final String shootingStyle;
    private final String brand;
    private final Integer fittingCount;
    private final Integer artistCount;
    private final Boolean homeCareAvailable;
    private final Long homeCareFee;

    public static VendorResponseDTO from(Vendor vendor) {
        VendorResponseDTOBuilder builder = VendorResponseDTO.builder()
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
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt());

        if (vendor instanceof WeddingHall weddingHall) {
            builder.capacity(weddingHall.getCapacity())
                    .hallCount(weddingHall.getHallCount())
                    .mealAvailable(weddingHall.isMealAvailable())
                    .parkingAvailable(weddingHall.isParkingAvailable());
        } else if (vendor instanceof Studio studio) {
            builder.outdoor(studio.isOutdoor())
                    .photographerCount(studio.getPhotographerCount())
                    .shootingStyle(studio.getShootingStyle());
        } else if (vendor instanceof Dress dress) {
            builder.brand(dress.getBrand())
                    .fittingCount(dress.getFittingCount());
        } else if (vendor instanceof Makeup makeup) {
            builder.artistCount(makeup.getArtistCount())
                    .homeCareAvailable(makeup.isHomeCareAvailable())
                    .homeCareFee(makeup.getHomeCareFee());
        }

        return builder.build();
    }
}
