package com.wedit.backend.api.agency.repository;

import com.wedit.backend.api.agency.entity.AgencyProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyProductRepository extends JpaRepository<AgencyProduct, Long> {
}
