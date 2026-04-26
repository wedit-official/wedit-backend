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
@Table(name = "dresses")
@DiscriminatorValue("DRESS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dress extends Vendor {

    private String brand;           // 드레스 브랜드

    private Integer fittingCount;   // 피팅 횟수

    @Builder
    public Dress(String name, String region, String fullAddress, String addressDetail,
                 String contactInfo, Double latitude, Double longitude,
                 String kakaoMapUrl, String website, String instagramUrl, String description,
                 String brand, Integer fittingCount) {
        super(name, region, fullAddress, addressDetail, contactInfo,
              latitude, longitude, kakaoMapUrl, website, instagramUrl, description);
        this.brand = brand;
        this.fittingCount = fittingCount;
    }

    @Override
    public VendorCategory getVendorCategory() {
        return VendorCategory.DRESS;
    }
}
