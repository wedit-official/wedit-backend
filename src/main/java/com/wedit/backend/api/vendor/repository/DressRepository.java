package com.wedit.backend.api.vendor.repository;

import com.wedit.backend.api.vendor.entity.Dress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DressRepository extends JpaRepository<Dress, Long> {
}
