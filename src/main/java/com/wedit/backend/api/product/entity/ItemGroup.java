package com.wedit.backend.api.product.entity;

import com.wedit.backend.api.vendor.entity.Vendor;
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
@Table(name = "item_groups")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false)
    private String name;                // 상품 그룹명 (예: 스튜디오 기본 패키지)

    @Column(columnDefinition = "TEXT")
    private String description;         // 그룹 설명

    private Long cachedMinPrice;        // 최저가 캐싱 (목록 조회 성능용)

    @Column(nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "itemGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    @Builder
    public ItemGroup(Vendor vendor, String name, String description) {
        this.vendor = vendor;
        this.name = name;
        this.description = description;
        this.cachedMinPrice = 0L;
        this.isDeleted = false;
        this.products = new ArrayList<>();
    }

    public void assignVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public void updateCachedMinPrice(Long newMinPrice) {
        this.cachedMinPrice = newMinPrice;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
