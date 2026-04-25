package com.wedit.backend.common.config.jwt;

import com.wedit.backend.api.member.jwt.filter.JwtAuthenticationProcessingFilter;
import com.wedit.backend.api.member.jwt.repository.RefreshTokenRepository;
import com.wedit.backend.api.member.jwt.service.JwtService;
import com.wedit.backend.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtService, memberRepository, refreshTokenRepository);
    }
}
