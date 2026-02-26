package com.wedit.backend.api.member.entity;

import com.wedit.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = true, length = 255)
    private String password;

    @Column(nullable = true, unique = true, length = 100)
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private SocialProvider socialProvider;

    @Column(nullable = true, length = 100)
    private String socialLoginId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = true)
    private LocalDate birthDate;

    @Column(nullable = true, length = 30)
    private String phoneNumber;

    @Column(nullable = true)
    private LocalDate weddingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    private SpouseType spouseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean deleted;

    private LocalDateTime deletedAt;

    public void updateOauthId(String newOauthId) {
        this.oauthId = newOauthId;
    }

    public Member update(String name) {
        this.name = name;
        return this;
    }

    public void updateProfile(LocalDate birthDate, String phoneNumber, LocalDate weddingDate, SpouseType spouseType) {
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.weddingDate = weddingDate;
        this.spouseType = spouseType;
    }

    public void markDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
