package com.wedit.backend.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseException extends RuntimeException {

    private HttpStatus statusCode;
    private String responseMessage;

    public BaseException(HttpStatus statusCode) {
        super(statusCode.getReasonPhrase());
        this.statusCode = statusCode;
        this.responseMessage = statusCode.getReasonPhrase();
    }

    public BaseException(HttpStatus statusCode, String responseMessage) {
        super(responseMessage);
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
    }

    public int getStatusCode() {
        return this.statusCode.value();
    }
}
