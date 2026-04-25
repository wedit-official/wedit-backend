package com.wedit.backend.api.member.jwt.service;

import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.jwt.entity.RefreshToken;
import com.wedit.backend.api.member.jwt.repository.RefreshTokenRepository;
import com.wedit.backend.api.member.repository.MemberRepository;
import com.wedit.backend.common.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void saveOrUpdateRefreshToken(Long memberId, String token, LocalDateTime expiresAt, String deviceInfo) {

        Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다. id= " + memberId));

        // 기존 리프레쉬 토큰 삭제 (단일 세션)
        refreshTokenRepository.deleteAllByMemberId(memberId);

        RefreshToken refreshToken = RefreshToken.builder()
                .member(member)
                .token(token)
                .expiresAt(expiresAt)
                .deviceInfo(deviceInfo)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteAllByMemberId(Long memberId) {
        refreshTokenRepository.deleteAllByMemberId(memberId);
    }

    public boolean isTokenExpired(RefreshToken refreshToken) {
        return refreshToken.getExpiresAt().isBefore(LocalDateTime.now());
    }
}
