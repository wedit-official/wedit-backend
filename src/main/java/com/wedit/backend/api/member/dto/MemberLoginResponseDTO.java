package com.wedit.backend.api.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberLoginResponseDTO {

    private final String accessToken;
    private final String refreshToken;
}
