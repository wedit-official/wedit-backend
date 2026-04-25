package com.wedit.backend.api.product.repository;

import com.wedit.backend.api.product.entity.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {
}
