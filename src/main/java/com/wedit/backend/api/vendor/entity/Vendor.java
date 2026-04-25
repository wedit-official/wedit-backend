package com.wedit.backend.api.vendor.entity;

import com.wedit.backend.api.product.entity.ItemGroup;
import com.wedit.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "vendors")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "vendor_type", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Vendor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;                // 업체 이름

    @Column(nullable = false)
    private String region;              // 소속 지역

    @Column(nullable = false)
    private String fullAddress;         // 전체 주소 (도로명 또는 지번)

    private String addressDetail;       // 상세 주소 (3층, 201호 등)

    private String contactInfo;         // 연락처

    private Double latitude;            // 위도

    private Double longitude;           // 경도

    private String kakaoMapUrl;         // 카카오맵 URL

    private String website;             // 업체 공식 사이트

    private String instagramUrl;        // 인스타그램 주소 (없으면 null)

    @Column(columnDefinition = "TEXT")
    private String description;         // 업체 소개

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemGroup> itemGroups = new ArrayList<>();

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordering ASC")
    private List<VendorMedia> mediaList = new ArrayList<>();

    protected Vendor(String name, String region, String fullAddress, String addressDetail,
                     String contactInfo, Double latitude, Double longitude,
                     String kakaoMapUrl, String website, String instagramUrl, String description) {
        this.name = name;
        this.region = region;
        this.fullAddress = fullAddress;
        this.addressDetail = addressDetail;
        this.contactInfo = contactInfo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.kakaoMapUrl = kakaoMapUrl;
        this.website = website;
        this.instagramUrl = instagramUrl;
        this.description = description;
        this.isActive = true;
        this.itemGroups = new ArrayList<>();
        this.mediaList = new ArrayList<>();
    }

    // 각 서브클래스가 자신의 업종을 반환
    public abstract VendorCategory getVendorCategory();

    public void addItemGroup(ItemGroup itemGroup) {
        itemGroups.add(itemGroup);
        itemGroup.assignVendor(this);
    }

    public void addMedia(VendorMedia media) {
        mediaList.add(media);
        media.assignVendor(this);
    }

    public void deactivate() {
        this.isActive = false;
    }
}
