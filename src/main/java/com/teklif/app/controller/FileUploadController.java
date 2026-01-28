package com.teklif.app.controller;

import com.teklif.app.dto.response.ApiResponse;
import com.teklif.app.dto.response.FileUploadResponse;
import com.teklif.app.entity.ProductFile;
import com.teklif.app.util.FileValidationUtil;
import com.teklif.app.util.ImageCompressionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Files", description = "File upload and serve endpoints")
public class FileUploadController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private final ImageCompressionUtil imageCompressionUtil;

    // ========== Upload Endpoints (Authenticated) ==========

    @PostMapping("/api/files/upload")
    @Operation(summary = "Upload file")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file
    ) {
        // Validate file
        FileValidationUtil.ValidationResult validation = FileValidationUtil.validateFile(file);
        if (!validation.valid) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(validation.error));
        }

        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = validation.extension;

            // Compress image if needed
            byte[] fileBytes;
            long finalFileSize;

            if (ImageCompressionUtil.isImage(file.getContentType())) {
                fileBytes = imageCompressionUtil.compressImage(file.getInputStream(), file.getContentType());
                finalFileSize = fileBytes.length;
                logCompression(file.getSize(), finalFileSize);
            } else {
                fileBytes = file.getBytes();
                finalFileSize = file.getSize();
            }

            ProductFile.FileType fileType = ProductFile.FileType.valueOf(validation.fileType);

            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path datePath = uploadPath.resolve(dateFolder);
            if (!Files.exists(datePath)) {
                Files.createDirectories(datePath);
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = datePath.resolve(uniqueFilename);

            // Save file
            Files.write(filePath, fileBytes);

            // Build relative file path for response
            String relativePath = dateFolder.replace("\\", "/") + "/" + uniqueFilename;

            FileUploadResponse response = FileUploadResponse.builder()
                    .fileName(originalFilename)
                    .filePath(relativePath)
                    .fileSize(finalFileSize)
                    .fileType(file.getContentType())
                    .category(fileType.name())
                    .extension(extension)
                    .uploadedAt(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }

    @PostMapping("/api/files/upload/multiple")
    @Operation(summary = "Upload multiple files")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> uploadFiles(
            @RequestParam("files") MultipartFile[] files
    ) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No files provided"));
        }

        List<FileUploadResponse> responses = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // Validate each file
                FileValidationUtil.ValidationResult validation = FileValidationUtil.validateFile(file);
                if (!validation.valid) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("File " + file.getOriginalFilename() + ": " + validation.error));
                }

                try {
                    // Create upload directory if not exists
                    Path uploadPath = Paths.get(uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    String originalFilename = file.getOriginalFilename();
                    String extension = validation.extension;

                    // Compress image if needed
                    byte[] fileBytes;
                    long finalFileSize;

                    if (ImageCompressionUtil.isImage(file.getContentType())) {
                        fileBytes = imageCompressionUtil.compressImage(file.getInputStream(), file.getContentType());
                        finalFileSize = fileBytes.length;
                    } else {
                        fileBytes = file.getBytes();
                        finalFileSize = file.getSize();
                    }

                    ProductFile.FileType fileType = ProductFile.FileType.valueOf(validation.fileType);

                    String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                    Path datePath = uploadPath.resolve(dateFolder);
                    if (!Files.exists(datePath)) {
                        Files.createDirectories(datePath);
                    }

                    String uniqueFilename = UUID.randomUUID().toString() + extension;
                    Path filePath = datePath.resolve(uniqueFilename);

                    // Save file
                    Files.write(filePath, fileBytes);

                    // Build relative file path for response
                    String relativePath = dateFolder.replace("\\", "/") + "/" + uniqueFilename;

                    FileUploadResponse response = FileUploadResponse.builder()
                            .fileName(originalFilename)
                            .filePath(relativePath)
                            .fileSize(finalFileSize)
                            .fileType(file.getContentType())
                            .category(fileType.name())
                            .extension(extension)
                            .uploadedAt(Instant.now())
                            .build();

                    responses.add(response);

                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("Failed to upload file " + file.getOriginalFilename() + ": " + e.getMessage()));
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responses));
    }

    @DeleteMapping("/api/files/delete")
    @Operation(summary = "Delete file")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam String filePath) {
        try {
            Path fullPath = Paths.get(uploadDir, filePath);

            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                return ResponseEntity.ok(ApiResponse.successWithMessage("File deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("File not found"));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete file: " + e.getMessage()));
        }
    }

    // ========== Serve Endpoint (Public) ==========

    @GetMapping("/files/uploads/**")
    @Operation(summary = "Serve uploaded file")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        try {
            // Get the path after /files/uploads/
            String requestURI = request.getRequestURI();
            String filePath = requestURI.substring("/files/uploads/".length());

            Path fullPath = Paths.get(uploadDir, filePath).normalize();

            // Security check: ensure path is within upload directory
            Path uploadPath = Paths.get(uploadDir).normalize().toAbsolutePath();
            if (!fullPath.toAbsolutePath().startsWith(uploadPath)) {
                return ResponseEntity.badRequest().build();
            }

            if (!Files.exists(fullPath) || !Files.isReadable(fullPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(fullPath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = Files.probeContentType(fullPath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fullPath.getFileName().toString() + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== Helper Methods ==========

    private void logCompression(long originalSize, long compressedSize) {
        if (originalSize > 0) {
            double ratio = ((originalSize - compressedSize) * 100.0) / originalSize;
            log.info("Image compression: {} -> {} bytes ({}% reduction)",
                    formatFileSize(originalSize),
                    formatFileSize(compressedSize),
                    String.format("%.1f", Math.max(0, ratio)));
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    @Data
    static class FileUploadRequest {
        private String fileName;
        private String category;
    }
}
