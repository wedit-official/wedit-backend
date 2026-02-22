package com.wedit.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    /// 200 OK
    FORM_LOGIN_SUCCESS(HttpStatus.OK, "폼 로그인 성공"),

    /// 201 CREATED
    MEMBER_SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입 성공"),

    /// 200 OK
    MEMBER_WITHDRAW_SUCCESS(HttpStatus.OK, "회원탈퇴 성공"),

    /// 200 OK
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "토큰 재발급 성공"),



    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
