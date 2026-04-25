package com.wedit.backend.api.product.entity;

import com.wedit.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "option_groups")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptionGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;                // 옵션 그룹명 (예: "촬영 시간", "드레스 스타일")

    /**
     * 기본 옵션(true) vs 추가 옵션(false)
     * - 기본 옵션: 상품 구성을 결정하는 필수 선택 (예: 촬영 시간 2h/3h/4h 중 택1)
     * - 추가 옵션: 선택적으로 추가 가능한 옵션 (예: 원본 파일, 포토북)
     */
    @Column(nullable = false)
    private boolean isMandatory;

    private Integer minSelectCount;     // 최소 선택 수 (기본 옵션이면 1 이상)

    private Integer maxSelectCount;     // 최대 선택 수 (단일 선택이면 1)

    @Column(nullable = false)
    private Integer ordering;           // 화면 표시 순서

    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordering ASC")
    private List<OptionDetail> optionDetails = new ArrayList<>();

    @Builder
    public OptionGroup(String name, boolean isMandatory,
                       Integer minSelectCount, Integer maxSelectCount, Integer ordering) {
        this.name = name;
        this.isMandatory = isMandatory;
        this.minSelectCount = minSelectCount != null ? minSelectCount : (isMandatory ? 1 : 0);
        this.maxSelectCount = maxSelectCount != null ? maxSelectCount : 1;
        this.ordering = ordering != null ? ordering : 0;
        this.optionDetails = new ArrayList<>();
    }

    public void assignProduct(Product product) {
        this.product = product;
    }

    public void addOptionDetail(OptionDetail detail) {
        this.optionDetails.add(detail);
        detail.assignOptionGroup(this);
    }
}
