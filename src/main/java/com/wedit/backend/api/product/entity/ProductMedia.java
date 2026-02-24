package com.wedit.backend.api.product.entity;

import com.wedit.backend.common.entity.BaseMedia;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "product_media")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMedia extends BaseMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder
    public ProductMedia(Product product, String fileKey, String url, Integer ordering) {
        super(url, fileKey, ordering);
        this.product = product;
    }

    public void assignProduct(Product product) {
        this.product = product;
    }
}
