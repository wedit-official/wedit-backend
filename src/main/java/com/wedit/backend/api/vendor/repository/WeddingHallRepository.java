package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.WeddingHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeddingHallRepository extends JpaRepository<WeddingHall, Long> {
}
