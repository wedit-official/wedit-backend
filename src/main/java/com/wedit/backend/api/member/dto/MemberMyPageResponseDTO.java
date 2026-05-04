package com.wedit.backend.api.member.dto;

public record MemberMyPageResponseDTO(
        String name,
        long scrapCount,
        long totalEstimateAmount    // TODO: 견적서 도메인 완성 후 계산, 현재 0 고정
) {
    public static MemberMyPageResponseDTO of(String name, long scrapCount) {
        return new MemberMyPageResponseDTO(name, scrapCount, 0L);
    }
}