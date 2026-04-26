package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.Dress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DressRepository extends JpaRepository<Dress, Long> {
}
