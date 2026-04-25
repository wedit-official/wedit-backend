package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.Makeup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MakeupRepository extends JpaRepository<Makeup, Long> {
}
