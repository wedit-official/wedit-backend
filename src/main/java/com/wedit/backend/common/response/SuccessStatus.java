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
    MEMBER_WITHDRAW_SUCCESS(HttpStatus.OK, "회원탈퇴 성공"),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "토큰 재발급 성공"),
    MEDIA_LIST_SUCCESS(HttpStatus.OK, "미디어 목록 조회 성공"),
    MEDIA_UPDATE_SUCCESS(HttpStatus.OK, "미디어 수정 성공"),
    AWS_S3_PRESIGNED_URL_SUCCESS(HttpStatus.OK, "S3 Presigned URL 발급 성공"),

    /// 201 CREATED
    MEMBER_SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입 성공"),
    MEDIA_ADD_SUCCESS(HttpStatus.CREATED, "미디어 추가 성공"),

    /// 204 NO CONTENT
    MEDIA_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "미디어 삭제 성공"),



    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
