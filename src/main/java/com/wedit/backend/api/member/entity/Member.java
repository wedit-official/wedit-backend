package com.wedit.backend.api.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = true, length = 255)
    private String password;

    @Column(nullable = true, unique = true, length = 100)
    private String oauthId;

    @Column(nullable = false, length = 50)
    private String name;

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

    public void markDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
