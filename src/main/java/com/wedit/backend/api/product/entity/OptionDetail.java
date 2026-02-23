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
    private String name;                // 옵션 이름

    @Column(nullable = false)
    private Long price;                 // 추가 가격

    // 수량 기반 옵션 필드
    private String unit;                // 단순 1회성이면 null (장, 벌, 시간 등)
    private Integer maxCount;           // 최대 선택 가능 수량 (제한 없으면 null)

    @Column(nullable = false)
    private boolean isSoldOut = false;  // 품절 여부

    @Column(nullable = false)
    private Integer ordering;

    @Builder
    public OptionDetail(String name, Long price, String unit, Integer maxCount, Integer ordering) {
        this.name = name;
        this.price = price != null ? price : 0L;
        this.unit = unit;
        this.maxCount = maxCount;
        this.ordering = ordering != null ? ordering : 0;
    }

    public void assignOptionGroup(OptionGroup optionGroup) {
        this.optionGroup = optionGroup;
    }

    public void toggleSoldOut() {
        this.isSoldOut = !this.isSoldOut;
    }

    // 견적서 계산 시 수량을 곱해야 하는 옵션인지 판단하기 위한 메서드
    public boolean isQuantityBased() {
        return this.unit != null;
    }
}
