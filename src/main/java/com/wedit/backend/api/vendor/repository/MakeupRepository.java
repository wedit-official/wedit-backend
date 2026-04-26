package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.Makeup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MakeupRepository extends JpaRepository<Makeup, Long> {
}
