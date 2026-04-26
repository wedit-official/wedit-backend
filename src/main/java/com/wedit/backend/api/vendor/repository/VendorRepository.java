package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByIdAndIsActiveTrue(Long id);

    List<Vendor> findAllByIsActiveTrue(Sort sort);
}
