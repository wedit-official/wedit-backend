package com.wedit.backend.api.member.jwt.service;

import com.wedit.backend.api.member.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final Long accessTokenExpirePeriod;
    private final Long refreshTokenExpirePeriod;
    private final RefreshTokenService refreshTokenService;

    public JwtService(@Value("${jwt.secretKey}") String secretKey,
                      @Value("${jwt.access.expiration}") Long accessTokenExpirePeriod,
                      @Value("${jwt.refresh.expiration}") Long refreshTokenExpirePeriod,
                      RefreshTokenService refreshTokenService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirePeriod = accessTokenExpirePeriod;
        this.refreshTokenExpirePeriod = refreshTokenExpirePeriod;
        this.refreshTokenService = refreshTokenService;
    }

    // Access Token 발급
    public String createAccessToken(Long memberId, String email, Role role) {

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessTokenExpirePeriod);

        return Jwts.builder()
                .setSubject(memberId.toString())    // sub
                .claim("email", email)
                .claim("role", role.name())
                .claim("type", "ACCESS")
                .setIssuedAt(now)                   // iat
                .setExpiration(expirationDate)      // exp
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Refresh Token 발급
    public String createRefreshToken(Long memberId) {

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + refreshTokenExpirePeriod);

        String token =  Jwts.builder()
                .setSubject(memberId.toString())    // sub
                .claim("type", "REFRESH")
                .setIssuedAt(now)                   // iat
                .setExpiration(expirationDate)      // exp
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        refreshTokenService.deleteAllByMemberId(memberId);

        refreshTokenService.saveOrUpdateRefreshToken(
                memberId,
                token,
                expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                null
        );

        return token;
    }
    
    // Access + Refresh Token 발급
    public Map<String, String> createAccessAndRefreshToken(Long memberId, String email, Role role) {

        String accessToken = createAccessToken(memberId, email, role);
        String refreshToken = createRefreshToken(memberId);

        log.debug("Access/Refresh Token 발급 완료 : {}", memberId);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    public void updateRefreshToken(Long memberId, String refreshToken) {

    }

    /***
     * 토큰 유효성 검증 메서드
     * @param token 검증할 토큰
     * @return 리턴 시 유효, false 시 무효
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다 : {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 토큰입니다 : {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 토큰입니다 : {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰입니다 : {}", e.getMessage());
        }
        return false;
    }
    
    // 토큰 클레임 추출
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    // 토큰에서 이메일 추출
    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(getClaimsFromToken(accessToken).get("email", String.class));
        } catch (Exception e) {
            log.error("토큰에서 이메일 추출 실패 : {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    // 토큰에서 권한 추출
    public Optional<String> extractRole(String accessToken) {
        try {
            return Optional.ofNullable(getClaimsFromToken(accessToken).get("role", String.class));
        } catch (Exception e) {
            log.error("토큰에서 권한 추출 실패 : {}", e.getMessage());
            return Optional.empty();
        }
    }
}
