package com.wedit.backend.api.scrap.entity;

import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scrap",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "vendor_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scrap extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    public static Scrap of(Member member, Vendor vendor) {
        Scrap scrap = new Scrap();
        scrap.member = member;
        scrap.vendor = vendor;
        return scrap;
    }
}
