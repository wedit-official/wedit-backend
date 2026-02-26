package com.wedit.backend.api.aws.service;

import com.wedit.backend.api.aws.dto.AwsS3RequestDTO;
import com.wedit.backend.api.aws.dto.AwsS3ResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsS3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.presign.expiration-minutes}")
    private Integer expirationMinutes;

    @Value("${cloud.aws.cloudfront.url}")
    private String cloudFrontBaseUrl;


    // Presigned URL 발급
    public AwsS3ResponseDTO generatePresignedUrl(AwsS3RequestDTO requestDTO) {

        String fileKey = buildFileKey(
                requestDTO.directory().getPath(),
                requestDTO.originalFileName()
        );

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(requestDTO.contentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

        String cloudFrontUrl = toCloudFrontUrl(fileKey);

        log.info("[S3] Presigned URL 발급 - directory: {}, fileKey: {}",
                requestDTO.directory(), fileKey);

        return new AwsS3ResponseDTO(presignedUrl, fileKey, cloudFrontUrl);
    }

    // S3 파일 단건 삭제
    public void deleteFile(String fileKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build());
            log.info("[S3] 파일 삭제 성공 - fileKey: {}", fileKey);
        } catch (Exception e) {
            log.error("[S3] 파일 삭제 실패 - fileKey: {}", fileKey, e);
        }
    }

    // https://cloudfront-domain/vendors/{UUID}.jpg
    public String toCloudFrontUrl(String fileKey) {

        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }

        return cloudFrontBaseUrl + "/" + fileKey;
    }

    // vendors/{UUID}.jpg
    private String buildFileKey(String directory, String originalFileName) {

        String extension = extractExtension(originalFileName);
        return directory + "/" + UUID.randomUUID() + extension;
    }

    // photo.jpg -> .jpg
    private String extractExtension(String fileName) {

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dotIndex).toLowerCase();
    }
}
