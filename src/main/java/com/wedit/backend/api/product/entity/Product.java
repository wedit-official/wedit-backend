package com.wedit.backend.api.product.entity;

import com.wedit.backend.api.agency.entity.Agency;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @Column(nullable = false)
    private String name;            // 상품 이름

    @Column(nullable = false)
    private Long basePrice = 0L;    // 기본 가격 0원 시작

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> tags = new ArrayList<>();  // UI 및 검색용 태그 (JSON)

    @Column(nullable = false)
    private boolean isVisible = false;  // 노출 제어 필드 (true: UI 노출)

    @Column(nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordering ASC")
    private List<OptionGroup> optionGroups = new ArrayList<>();

    @Builder
    public Product(ItemGroup itemGroup, Agency agency, String name, Long basePrice,
                List<String> tags) {
        this.itemGroup = itemGroup;
        this.agency = agency;
        this.name = name;
        this.basePrice = basePrice != null ? basePrice : 0L;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.isVisible = false;   // 최초 등록 시 임시저장 상태
        this.isDeleted = false;
        this.optionGroups = new ArrayList<>();
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

    // 옵션 그룹 추가
    public void addOptionGroup(OptionGroup optionGroup) {
        this.optionGroups.add(optionGroup);
        optionGroup.assignProduct(this);
    }
}
