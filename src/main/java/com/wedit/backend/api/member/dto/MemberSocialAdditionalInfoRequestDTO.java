package com.wedit.backend.api.member.dto;

import com.wedit.backend.api.member.entity.SpouseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MemberSocialAdditionalInfoRequestDTO {

    @NotNull
    private LocalDate birthDate;

    @NotBlank
    @Pattern(regexp = "^[0-9-]{8,20}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;

    private LocalDate weddingDate;

    @NotNull
    private SpouseType spouseType;
}
