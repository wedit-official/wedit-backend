package com.wedit.backend.api.member.jwt.filter;

import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.jwt.service.JwtService;
import com.wedit.backend.api.member.service.MemberService;
import com.wedit.backend.common.config.security.entity.SecurityMember;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    @Value("${jwt.access.header}")
    private String accessTokenHeader;

    private final JwtService jwtService;
    private final MemberService memberService;

    private static final String[] SWAGGER_URIS = {
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-ui.html",
    };

    /// 스웨거 관련 경로 필터링 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        
        for (String uri : SWAGGER_URIS) {
            if (requestURI.contains(uri)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> accessTokenOpt = extractToken(request, accessTokenHeader)
                .filter(jwtService::isTokenValid);

        accessTokenOpt.ifPresent(token -> jwtService.extractEmail(token)
                .flatMap(memberService::findActiveByEmail)
                .ifPresent(this::setAuthentication));

        filterChain.doFilter(request, response);
    }

    /// 토큰 추출 유틸 메서드
    private Optional<String> extractToken(HttpServletRequest request, String header) {

        String bearerToken = request.getHeader(header);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        
        return Optional.empty();
    }

    /// Member -> SecurityMember 로 생성 후 SecurityContext 에 등록
    private void setAuthentication(Member member) {

        SecurityMember securityMember = SecurityMember.from(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                securityMember, null, securityMember.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
