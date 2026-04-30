package com.wedit.backend.api.scrap.repository;

import com.wedit.backend.api.scrap.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    boolean existsByMemberIdAndVendorId(Long memberId, Long vendorId);

    Optional<Scrap> findByMemberIdAndVendorId(Long memberId, Long vendorId);

    long countByMemberId(Long memberId);
}
