package com.wedit.backend.api.scrap.repository;

import com.wedit.backend.api.scrap.entity.Scrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    boolean existsByMemberIdAndVendorId(Long memberId, Long vendorId);

    Optional<Scrap> findByMemberIdAndVendorId(Long memberId, Long vendorId);

    long countByMemberId(Long memberId);

    @Query("""
        SELECT s FROM Scrap s
        JOIN FETCH s.vendor v
        WHERE s.member.id = :memberId
        AND v.isActive = true
        ORDER BY s.createdAt DESC
    """)
    Page<Scrap> findScrapsByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    @Query("""
        SELECT s FROM Scrap s
        JOIN FETCH s.vendor v
        WHERE s.member.id = :memberId
        AND v.isActive = true
        AND (
            (:category = 'WEDDING_HALL' AND TYPE(v) = WeddingHall) OR
            (:category = 'STUDIO'       AND TYPE(v) = Studio)       OR
            (:category = 'DRESS'        AND TYPE(v) = Dress)        OR
            (:category = 'MAKEUP'       AND TYPE(v) = Makeup)
        )
        ORDER BY s.createdAt DESC
    """)
    Page<Scrap> findScrapsByMemberIdAndCategory(
            @Param("memberId") Long memberId,
            @Param("category") String category,
            Pageable pageable
    );
}