package com.wedit.backend.api.vendor.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "makeups")
@DiscriminatorValue("MAKEUP")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Makeup extends Vendor {

    private Integer artistCount;            // 아티스트 수

    private boolean isHomeCareAvailable;    // 출장 가능 여부

    private Long homeCareFee;               // 출장 기본 비용

    @Builder
    public Makeup(String name, String region, String fullAddress, String addressDetail,
                  String contactInfo, Double latitude, Double longitude,
                  String kakaoMapUrl, String website, String instagramUrl, String description,
                  Integer artistCount, boolean isHomeCareAvailable, Long homeCareFee) {
        super(name, region, fullAddress, addressDetail, contactInfo,
              latitude, longitude, kakaoMapUrl, website, instagramUrl, description);
        this.artistCount = artistCount;
        this.isHomeCareAvailable = isHomeCareAvailable;
        this.homeCareFee = homeCareFee;
    }

    @Override
    public VendorCategory getVendorCategory() {
        return VendorCategory.MAKEUP;
    }
}
