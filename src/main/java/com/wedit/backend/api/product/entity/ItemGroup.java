package com.wedit.backend.api.product.entity;

import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Long cachedMinPrice;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Builder
    public ItemGroup(Vendor vendor, String name, String description) {
        this.vendor = vendor;
        this.name = name;
        this.description = description;
        this.cachedMinPrice = 0L;
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
