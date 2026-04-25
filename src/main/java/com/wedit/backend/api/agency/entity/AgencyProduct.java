package com.wedit.backend.api.agency.entity;

import com.wedit.backend.api.product.entity.Product;
import com.wedit.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "agency_products",
    uniqueConstraints = @UniqueConstraint(columnNames = {"agency_id", "product_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgencyProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long agencyPrice;       // 대행업체별 판매 가격

    @Column(nullable = false)
    private boolean isActive = true;

    @Builder
    public AgencyProduct(Agency agency, Product product, Long agencyPrice) {
        this.agency = agency;
        this.product = product;
        this.agencyPrice = agencyPrice;
        this.isActive = true;
    }

    public void updatePrice(Long newPrice) {
        this.agencyPrice = newPrice;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
