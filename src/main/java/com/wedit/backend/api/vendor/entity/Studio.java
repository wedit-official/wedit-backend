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
@Table(name = "studios")
@DiscriminatorValue("STUDIO")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Studio extends Vendor {

    private boolean isOutdoor;          // 야외 촬영 가능 여부

    private Integer photographerCount; // 작가 수

    private String shootingStyle;       // 촬영 스타일 설명

    @Builder
    public Studio(String name, String region, String fullAddress, String addressDetail,
                  String contactInfo, Double latitude, Double longitude,
                  String kakaoMapUrl, String website, String instagramUrl, String description,
                  boolean isOutdoor, Integer photographerCount, String shootingStyle) {
        super(name, region, fullAddress, addressDetail, contactInfo,
              latitude, longitude, kakaoMapUrl, website, instagramUrl, description);
        this.isOutdoor = isOutdoor;
        this.photographerCount = photographerCount;
        this.shootingStyle = shootingStyle;
    }

    @Override
    public VendorCategory getVendorCategory() {
        return VendorCategory.STUDIO;
    }

    public void updateDetails(boolean isOutdoor, Integer photographerCount, String shootingStyle) {
        this.isOutdoor = isOutdoor;
        this.photographerCount = photographerCount;
        this.shootingStyle = shootingStyle;
    }
}
