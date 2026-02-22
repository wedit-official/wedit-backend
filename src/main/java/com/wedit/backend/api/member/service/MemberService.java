package com.wedit.backend.api.member.service;

import com.wedit.backend.api.member.dto.MemberLoginRequestDTO;
import com.wedit.backend.api.member.dto.MemberLoginResponseDTO;
import com.wedit.backend.api.member.dto.MemberSignupRequestDTO;
import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.entity.Role;
import com.wedit.backend.api.member.jwt.service.JwtService;
import com.wedit.backend.api.member.jwt.service.RefreshTokenService;
import com.wedit.backend.api.member.repository.MemberRepository;
import com.wedit.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void signup(MemberSignupRequestDTO dto) {
        if (memberRepository.existsByEmailAndDeletedFalse(dto.getEmail())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        Member member = Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .role(Role.ROLE_USER)
                .build();

        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberLoginResponseDTO login(MemberLoginRequestDTO dto) {
        Member member = memberRepository.findByEmailAndDeletedFalse(dto.getEmail())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        if (member.getPassword() == null || !passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        Map<String, String> tokens = jwtService.createAccessAndRefreshToken(
                member.getId(),
                member.getEmail(),
                member.getRole()
        );

        return MemberLoginResponseDTO.builder()
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .build();
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
        if (member.isDeleted()) {
            return;
        }

        refreshTokenService.deleteAllByMemberId(memberId);
        member.markDeleted();
    }

    @Transactional
    public MemberLoginResponseDTO reissueTokens(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        return refreshTokenService.findByToken(refreshToken)
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(token -> {
                    Member member = token.getMember();
                    if (member.isDeleted()) {
                        throw new NotFoundException("존재하지 않는 사용자입니다.");
                    }
                    Map<String, String> tokens = jwtService.createAccessAndRefreshToken(
                            member.getId(),
                            member.getEmail(),
                            member.getRole()
                    );
                    return MemberLoginResponseDTO.builder()
                            .accessToken(tokens.get("accessToken"))
                            .refreshToken(tokens.get("refreshToken"))
                            .build();
                })
                .orElseThrow(() -> new NotFoundException("저장된 리프레시 토큰이 없습니다."));
    }
}
