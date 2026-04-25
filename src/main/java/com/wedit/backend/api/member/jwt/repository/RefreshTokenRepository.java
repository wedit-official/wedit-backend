package com.wedit.backend.api.member.jwt.repository;

import com.wedit.backend.api.member.jwt.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 토큰 값으로 조회
    Optional<RefreshToken> findByToken(String token);

    // 특정 회원 ID의 모든 리프레시 토큰 삭제
    void deleteAllByMemberId(Long memberId);

    // 특정 회원 ID의 리프레시 토큰 리스트 조회
    List<RefreshToken> findAllByMemberId(Long memberId);

    // 회원 ID와 토큰값으로 특정 리프레시 토큰 조회 (세션 검증 용)
    Optional<RefreshToken> findByMemberIdAndToken(Long memberId, String token);

    // 리프레쉬 토큰 만료 이전 또는 이후 토큰 리스트 조회 (만료 토큰 삭제 용)
    List<RefreshToken> findByExpiresAtBefore(LocalDateTime expiresAt);

    // 리프레쉬 토큰 만료 이전 토큰 리스트 조회 (활성 토큰 관리 용)
    List<RefreshToken> findByExpiresAtAfter(LocalDateTime expiresAt);
}
