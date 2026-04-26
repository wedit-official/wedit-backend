package com.wedit.backend.api.member.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedit.backend.common.response.ApiResponse;
import com.wedit.backend.common.response.ErrorStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

// @Component 제거 — Spring Framework 7이 컴포넌트 스캔 중 AuthenticationEntryPoint.class
// 파일을 ASM 리소스로 직접 열려다 FileNotFoundException을 던지는 문제를 방지한다.
// SecurityConfig에서 @Bean 으로 직접 등록한다.
public class FilterExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /// 인증 실패 시 호출 (ex. 토큰이 유효하지 않거나 없는 경우)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorStatus.UNAUTHORIZED_USER.getMessage());
    }

    /// 인가 실패 시 호출 (ex. 권한 없음)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        setErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ErrorStatus.FORBIDDEN_RESOURCE_ACCESS.getMessage());
    }

    private void setErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {

        response.setStatus(statusCode);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse = ApiResponse.fail(statusCode, message);
        String json = objectMapper.writeValueAsString(apiResponse);

        response.getWriter().write(json);
    }
}
