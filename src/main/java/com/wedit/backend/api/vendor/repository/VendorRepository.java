package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
}
