package com.wedit.backend.api.scrap.dto;

public record ScrapToggleResponseDTO(
        // true: 스크랩 추가, false: 스크랩 취소
        boolean isScraped
) {
    public static ScrapToggleResponseDTO of(boolean isScraped) {
        return new ScrapToggleResponseDTO(isScraped);
    }
}
