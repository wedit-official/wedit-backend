package com.wedit.backend.api.vendor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorMediaRequestDTO {

    @NotBlank
    @Size(max = 500)
    private String url;

    @PositiveOrZero
    private Integer ordering;

    private boolean thumbnail;
}
