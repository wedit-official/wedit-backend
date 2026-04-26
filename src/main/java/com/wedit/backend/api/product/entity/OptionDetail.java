package com.wedit.backend.api.product.entity;

import com.wedit.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "option_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptionDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_id", nullable = false)
    private OptionGroup optionGroup;

    @Column(nullable = false)
    private String name;                // 옵션 항목명 (예: "2시간", "A라인 드레스")

    @Column(nullable = false)
    private Long price;                 // 추가 금액 (기본 포함이면 0)

    private String unit;                // 수량 단위 (장, 벌, 시간 등) - null이면 단순 선택 옵션

    private Integer maxCount;           // 최대 선택 수량 - null이면 제한 없음

    @Column(nullable = false)
    private boolean isSoldOut = false;  // 품절 여부

    @Column(nullable = false)
    private Integer ordering;           // 화면 표시 순서

    @Builder
    public OptionDetail(String name, Long price, String unit, Integer maxCount, Integer ordering) {
        this.name = name;
        this.price = price != null ? price : 0L;
        this.unit = unit;
        this.maxCount = maxCount;
        this.isSoldOut = false;
        this.ordering = ordering != null ? ordering : 0;
    }

    public void assignOptionGroup(OptionGroup optionGroup) {
        this.optionGroup = optionGroup;
    }

    public void toggleSoldOut() {
        this.isSoldOut = !this.isSoldOut;
    }

    // 수량을 곱해서 견적을 계산해야 하는 옵션인지 여부
    public boolean isQuantityBased() {
        return this.unit != null;
    }
}
