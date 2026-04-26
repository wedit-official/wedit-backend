package com.wedit.backend.common.config.jwt;

import com.wedit.backend.api.member.jwt.filter.JwtAuthenticationProcessingFilter;
import com.wedit.backend.api.member.jwt.service.JwtService;
import com.wedit.backend.api.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtService jwtService;
    private final MemberService memberService;

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtService, memberService);
    }
}
