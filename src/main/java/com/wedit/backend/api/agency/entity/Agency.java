package com.wedit.backend.api.agency.entity;

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
@Table(name = "agencies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agency extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;            // 대행업체 이름

    private String phone;           // 대행업체 연락처

    private String website;         // 대행업체 사이트

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AgencyProduct> agencyProducts = new ArrayList<>();

    @Builder
    public Agency(String name, String phone, String website) {
        this.name = name;
        this.phone = phone;
        this.website = website;
        this.isActive = true;
        this.agencyProducts = new ArrayList<>();
    }

    public void deactivate() {
        this.isActive = false;
    }
}
