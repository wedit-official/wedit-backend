package com.wedit.backend.api.product.repository;

import com.wedit.backend.api.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
