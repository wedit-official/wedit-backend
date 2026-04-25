package com.wedit.backend.api.product.entity;

import com.wedit.backend.api.agency.entity.AgencyProduct;
import com.wedit.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_group_id", nullable = false)
    private ItemGroup itemGroup;

    @Column(nullable = false)
    private String name;                // 상품명

    @Column(nullable = false)
    private Long basePrice = 0L;        // 기본 가격

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "text")
    private List<String> tags = new ArrayList<>();  // 검색/UI용 태그

    @Column(nullable = false)
    private boolean isVisible = false;  // 노출 여부 (false = 임시저장)

    @Column(nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordering ASC")
    private List<OptionGroup> optionGroups = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AgencyProduct> agencyProducts = new ArrayList<>();  // 판매 대행업체 목록

    @Builder
    public Product(ItemGroup itemGroup, String name, Long basePrice, List<String> tags) {
        this.itemGroup = itemGroup;
        this.name = name;
        this.basePrice = basePrice != null ? basePrice : 0L;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.isVisible = false;
        this.isDeleted = false;
        this.optionGroups = new ArrayList<>();
        this.agencyProducts = new ArrayList<>();
    }

    public void publish() {
        this.isVisible = true;
    }

    public void hide() {
        this.isVisible = false;
    }

    public void delete() {
        this.isDeleted = true;
        this.isVisible = false;
    }

    public void addOptionGroup(OptionGroup optionGroup) {
        this.optionGroups.add(optionGroup);
        optionGroup.assignProduct(this);
    }

    public void addAgencyProduct(AgencyProduct agencyProduct) {
        this.agencyProducts.add(agencyProduct);
    }
}
