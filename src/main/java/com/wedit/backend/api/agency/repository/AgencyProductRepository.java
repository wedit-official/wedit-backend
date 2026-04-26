package com.wedit.backend.api.agency.repository;

import com.wedit.backend.api.agency.entity.AgencyProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgencyProductRepository extends JpaRepository<AgencyProduct, Long> {
}
