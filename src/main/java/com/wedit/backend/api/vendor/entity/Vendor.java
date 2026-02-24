package com.wedit.backend.api.vendor.entity;

import com.wedit.backend.api.product.entity.ItemGroup;
import com.wedit.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "vendors")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vendor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;            // 업체 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorCategory category;    // 업종 (웨딩홀/드레스/메이크업/스튜디오)

    @Column(nullable = false)
    private String region;          // 소속 지역

    @Column(nullable = false)
    private String fullAddress;     // 업체 전체 주소 (도로명 또는 지번)

    private String addressDetail;   // 업체 상세 주소 (3층, 201호 등)

    private String contactInfo;     // 업체 연락처

    private Double latitude;        // 위도

    private Double longitude;       // 경도

    private String kakaoMapUrl;     // 카카오맵 URL

    @Column(columnDefinition = "TEXT")
    private String description;     // 업체 소개

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemGroup> itemGroups = new ArrayList<>();

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordering ASC")
    private List<VendorMedia> mediaList = new ArrayList<>();

    @Builder
    public Vendor(String name, VendorCategory category, String region, String fullAddress,
                  String addressDetail, String contactInfo, Double latitude, Double longitude,
                  String kakaoMapUrl, String description) {
        this.name = name;
        this.category = category;
        this.region = region;
        this.fullAddress = fullAddress;
        this.addressDetail = addressDetail;
        this.contactInfo = contactInfo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.kakaoMapUrl = kakaoMapUrl;
        this.description = description;
        this.isActive = true;
        this.itemGroups = new ArrayList<>();
        this.mediaList = new ArrayList<>();
    }

    public void addItemGroup(ItemGroup itemGroup) {
        itemGroups.add(itemGroup);
        itemGroup.assignVendor(this);
    }

    public void addMedia(VendorMedia media) {
        this.mediaList.add(media);
        media.assignVendor(this);
    }

    public void deactivate() {
        this.isActive = false;
    }
}
