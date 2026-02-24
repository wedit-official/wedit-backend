package com.wedit.backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseMedia extends BaseTimeEntity {

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private Integer ordering;

    protected BaseMedia(String url, Integer ordering) {
        this.url = url;
        this.ordering = ordering != null ? ordering : 0;
    }
}
