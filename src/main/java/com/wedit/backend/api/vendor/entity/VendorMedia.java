package com.wedit.backend.api.vendor.entity;

import com.wedit.backend.common.entity.BaseMedia;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "vendor_media")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VendorMedia extends BaseMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    private boolean isThumbnail;

    @Builder // 클래스가 아닌 생성자 레벨에 붙임
    public VendorMedia(Vendor vendor, String url, String fileKey, Integer ordering, boolean isThumbnail) {
        super(url, fileKey, ordering);
        this.vendor = vendor;
        this.isThumbnail = isThumbnail;
    }

    public void assignVendor(Vendor vendor) {
        this.vendor = vendor;
    }
}
