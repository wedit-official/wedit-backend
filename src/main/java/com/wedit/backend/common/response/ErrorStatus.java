package com.wedit.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {

    /// 400 BAD REQUEST
    BAD_REQUEST_MISSING_PARAM(HttpStatus.BAD_REQUEST, "요청 값이 입력되지 않았습니다."),

    /// 401 UNAUTHORIZED
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

    /// 403 FORBIDDEN
    FORBIDDEN_RESOURCE_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    /// 404 NOT FOUND
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),

    /// 409 CONFLICT
    CONFLICT_DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "중복된 리소스가 존재합니다."),

    /// 415 UNSUPPORTED MEDIA TYPE
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 Content-Type 입니다."),

    /// 500 SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    /// 503 SERVICE UNAVAILABLE
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서버에 연결할 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
