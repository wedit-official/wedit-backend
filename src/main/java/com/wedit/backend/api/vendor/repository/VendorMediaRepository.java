package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.VendorMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorMediaRepository extends JpaRepository<VendorMedia, Long> {

    List<VendorMedia> findByVendorId(Long vendorId);

    Optional<VendorMedia> findByVendorIdAndIsThumbnailTrue(Long vendorId);
}
