package com.wedit.backend.api.aws.controller;

import com.wedit.backend.api.aws.dto.AwsS3RequestDTO;
import com.wedit.backend.api.aws.dto.AwsS3ResponseDTO;
import com.wedit.backend.api.aws.service.AwsS3Service;
import com.wedit.backend.common.response.ApiResponse;
import com.wedit.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AWS S3", description = "AWS S3 미디어 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/s3")
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    @Operation(summary = "Presigned URL 단건 발급",
            description = "이미지 업로드를 위한 AWS S3 Presigned URL를 발급합니다. 클라이언트는 응답받은 URL로 PUT 요청을 보내 이미지를 업로드해야 합니다.")
    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<AwsS3ResponseDTO>> generatePresignedUrl(
            @Valid @RequestBody AwsS3RequestDTO requestDTO
    ) {
        AwsS3ResponseDTO responseDTO = awsS3Service.generatePresignedUrl(requestDTO);

        return ApiResponse.success(SuccessStatus.AWS_S3_PRESIGNED_URL_SUCCESS, responseDTO);
    }
}
