package com.wedit.backend.api.vendor.entity;

import com.wedit.backend.common.entity.BaseMedia;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@Table(name = "vendor_media")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class VendorMedia extends BaseMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    private boolean isThumbnail;

    public void assignVendor(Vendor vendor) {
        this.vendor = vendor;
    }
}
