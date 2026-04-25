package com.wedit.backend.api.vendor.dto;

import com.wedit.backend.api.vendor.entity.VendorCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorUpsertRequestDTO {

    @NotNull
    private VendorCategory category;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String region;

    @NotBlank
    @Size(max = 255)
    private String fullAddress;

    @Size(max = 255)
    private String addressDetail;

    @Size(max = 50)
    private String contactInfo;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @Size(max = 500)
    private String kakaoMapUrl;

    @Size(max = 500)
    private String website;

    @Size(max = 500)
    private String instagramUrl;

    @Size(max = 2000)
    private String description;

    @PositiveOrZero
    private Integer capacity;

    @PositiveOrZero
    private Integer hallCount;

    private Boolean mealAvailable;

    private Boolean parkingAvailable;

    private Boolean outdoor;

    @PositiveOrZero
    private Integer photographerCount;

    @Size(max = 255)
    private String shootingStyle;

    @Size(max = 100)
    private String brand;

    @PositiveOrZero
    private Integer fittingCount;

    @PositiveOrZero
    private Integer artistCount;

    private Boolean homeCareAvailable;

    @PositiveOrZero
    private Long homeCareFee;
}
