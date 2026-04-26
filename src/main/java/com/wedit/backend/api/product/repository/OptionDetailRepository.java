package com.wedit.backend.api.product.repository;

import com.wedit.backend.api.product.entity.OptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionDetailRepository extends JpaRepository<OptionDetail, Long> {
}
