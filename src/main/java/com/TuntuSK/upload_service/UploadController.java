package com.TuntuSK.upload_service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {
    public S3Service s3service;
    // spring will automatically inject our S3service here(Dependency Injection):
    public UploadController(S3Service s3Service) {
        this.s3service = s3Service;

    }

    @PostMapping
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }
        String resultMessage = s3service.uploadFile(file);

        return ResponseEntity.ok(resultMessage);
    }
}