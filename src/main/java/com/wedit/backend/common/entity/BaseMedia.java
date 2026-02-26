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
    private String fileKey;

    @Column(nullable = false)
    private Integer ordering;

    protected BaseMedia(String url, String fileKey, Integer ordering) {
        this.url = url;
        this.fileKey = fileKey;
        this.ordering = ordering != null ? ordering : 0;
    }

    public void updateOrdering(int ordering) {
        this.ordering = ordering;
    }
}
