package com.wedit.backend.api.vendor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wedit.backend.api.vendor.entity.Dress;
import com.wedit.backend.api.vendor.entity.Makeup;
import com.wedit.backend.api.vendor.entity.Studio;
import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.api.vendor.entity.WeddingHall;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VendorSubtypeResponseDTO {

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

    public static VendorSubtypeResponseDTO from(Vendor vendor) {
        VendorSubtypeResponseDTOBuilder builder = VendorSubtypeResponseDTO.builder();

        if (vendor instanceof WeddingHall weddingHall) {
            return builder.capacity(weddingHall.getCapacity())
                    .hallCount(weddingHall.getHallCount())
                    .mealAvailable(weddingHall.isMealAvailable())
                    .parkingAvailable(weddingHall.isParkingAvailable())
                    .build();
        }

        if (vendor instanceof Studio studio) {
            return builder.outdoor(studio.isOutdoor())
                    .photographerCount(studio.getPhotographerCount())
                    .shootingStyle(studio.getShootingStyle())
                    .build();
        }

        if (vendor instanceof Dress dress) {
            return builder.brand(dress.getBrand())
                    .fittingCount(dress.getFittingCount())
                    .build();
        }

        Makeup makeup = (Makeup) vendor;
        return builder.artistCount(makeup.getArtistCount())
                .homeCareAvailable(makeup.isHomeCareAvailable())
                .homeCareFee(makeup.getHomeCareFee())
                .build();
    }
}
