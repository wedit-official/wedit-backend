package com.wedit.backend.api.aws.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AWS S3 Presigned URL 발급 응답 DTO")
public record AwsS3ResponseDTO(
        String presignedUrl,    // 클라이언트가 PUT 요청할 S3 URL
        String fileKey,         // DB 저장용 S3 객체 키
        String cloudFrontUrl    // 업로드 완료 후 조회용 URL
) {
}
