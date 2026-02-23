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
    private String name;                // 옵션 그룹 이름

    @Column(nullable = false)
    private boolean isMandatory;        // 필수 선택 여부

    private Integer minSelectCount;     // 최소 선택 개수 (필수면 1)
    private Integer maxSelectCount;     // 최대 선택 개수 (라디오면 1, 이외 99)

    @Column(nullable = false)
    private Integer ordering;           // 정렬 순서


    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordering ASC")
    private List<OptionDetail> optionDetails;

    @Builder
    public OptionGroup(String name, boolean isMandatory, Integer minSelectCount, Integer maxSelectCount,
                    Integer ordering) {
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

    // 옵션 상세 추가
    public void addOptionDetail(OptionDetail detail) {
        this.optionDetails.add(detail);
        detail.assignOptionGroup(this);
    }
}
