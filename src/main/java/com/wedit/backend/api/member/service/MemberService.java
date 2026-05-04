package com.wedit.backend.api.member.service;

import com.wedit.backend.api.member.dto.*;
import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.entity.Role;
import com.wedit.backend.api.member.jwt.service.JwtService;
import com.wedit.backend.api.member.jwt.service.RefreshTokenService;
import com.wedit.backend.api.member.repository.MemberRepository;
import com.wedit.backend.api.scrap.entity.Scrap;
import com.wedit.backend.api.scrap.repository.ScrapRepository;
import com.wedit.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final ScrapRepository scrapRepository;

    @Transactional
    public void signup(MemberSignupRequestDTO dto) {
        if (memberRepository.existsByEmailAndDeletedFalse(dto.getEmail())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        Member member = Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .birthDate(dto.getBirthDate())
                .phoneNumber(dto.getPhoneNumber())
                .weddingDate(dto.getWeddingDate())
                .spouseType(dto.getSpouseType())
                .role(Role.ROLE_USER)
                .build();

        memberRepository.save(member);
    }

    @Transactional
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
    public void saveSocialAdditionalInfo(Long memberId, MemberSocialAdditionalInfoRequestDTO dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        if (member.isDeleted()) {
            throw new NotFoundException("존재하지 않는 사용자입니다.");
        }

        if (member.getOauthId() == null) {
            throw new IllegalStateException("소셜 로그인 회원만 추가 정보를 저장할 수 있습니다.");
        }

        member.updateProfile(
                dto.getBirthDate(),
                dto.getPhoneNumber(),
                dto.getWeddingDate(),
                dto.getSpouseType()
        );
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

    @Transactional(readOnly = true)
    public MemberMyPageResponseDTO getMyPage(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        if (member.isDeleted()) {
            throw new NotFoundException("존재하지 않는 사용자입니다.");
        }

        long scrapCount = scrapRepository.countByMemberId(memberId);

        return MemberMyPageResponseDTO.of(member.getName(), scrapCount);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findActiveByEmail(String email) {
        return memberRepository.findByEmailAndDeletedFalse(email);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findActiveByOauthId(String oauthId) {
        return memberRepository.findByOauthIdAndDeletedFalse(oauthId);
    }

    @Transactional
    public Member saveOrUpdateOauthMember(String socialProvider, String socialId, String name, String email) {
        String oauthId = socialProvider + "_" + socialId;

        Member member = memberRepository.findByOauthIdAndDeletedFalse(oauthId)
                .map(entity -> entity.update(name))
                .orElseGet(() -> Member.builder()
                        .oauthId(oauthId)
                        .name(name)
                        .email(email)
                        .password("OAUTH_USER")
                        .role(Role.ROLE_USER)
                        .build());

        return memberRepository.save(member);
    }
}