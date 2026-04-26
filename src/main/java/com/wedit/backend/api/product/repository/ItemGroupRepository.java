package com.wedit.backend.api.product.repository;

import com.wedit.backend.api.product.entity.ItemGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemGroupRepository extends JpaRepository<ItemGroup, Long> {
}
