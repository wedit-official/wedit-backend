package com.wedit.backend.api.member.jwt.entity;

import com.wedit.backend.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 만료시각 (추후 Batch 작업으로 동면 세션 삭제 로직에 사용)
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 접속 기기 정보 (UUID, 추후 다중세션 지원 시 사용)
    private String deviceInfo;

    public void updateToken(String newToken, LocalDateTime newExpiresAt) {
        this.token = newToken;
        this.expiresAt = newExpiresAt;
    }
}
