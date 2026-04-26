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
@Table(name = "wedding_halls")
@DiscriminatorValue("WEDDING_HALL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeddingHall extends Vendor {

    private Integer capacity;           // 수용 인원

    private Integer hallCount;          // 홀 수

    private boolean mealAvailable;      // 식사 제공 여부

    private boolean parkingAvailable;   // 주차 가능 여부

    @Builder
    public WeddingHall(String name, String region, String fullAddress, String addressDetail,
                       String contactInfo, Double latitude, Double longitude,
                       String kakaoMapUrl, String website, String instagramUrl, String description,
                       Integer capacity, Integer hallCount, boolean mealAvailable, boolean parkingAvailable) {
        super(name, region, fullAddress, addressDetail, contactInfo,
              latitude, longitude, kakaoMapUrl, website, instagramUrl, description);
        this.capacity = capacity;
        this.hallCount = hallCount;
        this.mealAvailable = mealAvailable;
        this.parkingAvailable = parkingAvailable;
    }

    @Override
    public VendorCategory getVendorCategory() {
        return VendorCategory.WEDDING_HALL;
    }

    public void updateDetails(Integer capacity, Integer hallCount, boolean mealAvailable, boolean parkingAvailable) {
        this.capacity = capacity;
        this.hallCount = hallCount;
        this.mealAvailable = mealAvailable;
        this.parkingAvailable = parkingAvailable;
    }
}
