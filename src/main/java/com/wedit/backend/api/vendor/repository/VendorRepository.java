package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.Vendor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByIdAndIsActiveTrue(Long id);

    List<Vendor> findAllByIsActiveTrue(Sort sort);

    @Query("select distinct v from Vendor v left join fetch v.mediaList where v.isActive = true order by v.id desc")
    List<Vendor> findActiveDetails();

    @Query("select distinct v from Vendor v left join fetch v.mediaList order by v.id desc")
    List<Vendor> findAllDetails();

    @Query("select distinct v from Vendor v left join fetch v.mediaList where v.id = :id and v.isActive = true")
    Optional<Vendor> findActiveDetailById(@Param("id") Long id);

    @Query("select distinct v from Vendor v left join fetch v.mediaList where v.id = :id")
    Optional<Vendor> findDetailById(@Param("id") Long id);
}
