package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.Studio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudioRepository extends JpaRepository<Studio, Long> {
}
