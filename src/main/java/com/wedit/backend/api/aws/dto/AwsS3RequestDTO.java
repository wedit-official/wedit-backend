package com.wedit.backend.api.aws.dto;

import com.wedit.backend.api.aws.enums.AwsS3Directory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "AWS S3 Presigned URL 발급 요청 DTO")
public record AwsS3RequestDTO(

        @NotNull(message = "디렉토리명은 필수입니다. (vendors, products)")
        AwsS3Directory directory,   // vendors, products

        @NotBlank(message = "원본 파일명은 필수입니다.")
        String originalFileName,    // 파일 원본 이름

        @NotBlank(message = "Content-Type은 필수입니다. (image/jpeg)")
        String contentType         // MIME 타입

//        @NotNull(message = "Content-Length는 필수입니다.")
//        @Positive(message = "Content-Length는 음수일 수 없습니다.")
//        Long contentLength          // 컨텐츠 크기
) { }
