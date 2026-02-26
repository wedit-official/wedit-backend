package com.wedit.backend.api.aws.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AwsS3Directory {

    VENDORS("vendors"),
    PRODUCTS("products"),

    ;

    private final String path;
}
