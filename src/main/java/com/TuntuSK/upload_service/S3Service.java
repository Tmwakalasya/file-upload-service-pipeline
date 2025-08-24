package com.TuntuSK.upload_service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3Service {
    private S3Client s3Client;
    private final String bucketName;
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    @Autowired
    public S3Service(S3Client s3Client,@Value("${app.aws.s3.bucket-name}") String bucketName ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "-" + multipartFile.getOriginalFilename();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .contentType(multipartFile.getContentType())
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(),
                multipartFile.getSize()));
        return uniqueFileName;
    }

    public String generatePresignedGetUrl(String filepath) {
        try (S3Presigner presigner = S3Presigner.create()) {
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filepath)
                    .build();
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(objectRequest)
                    .build();
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(getObjectPresignRequest);
            logger.info("Presigned URL: [{}]", presignedRequest.url().toString());
            logger.info("HTTP method: [{}]", presignedRequest.httpRequest().toString());
            return presignedRequest.url().toExternalForm();
        }

    }
    public String generatePresignedPostUrl(String filePath, Map<String, String>metadata) {

        try (S3Presigner presigner = S3Presigner.create()){
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .metadata(metadata)
                    .build();
            PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(putRequest)
                    .build();
            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(putObjectPresignRequest);
            logger.info("Presigned URL: [{}]", presignedRequest.url().toString());
            logger.info("HTTP method: [{}]", presignedRequest.httpRequest().toString());
            return presignedRequest.url().toExternalForm();
        }
    }
}
