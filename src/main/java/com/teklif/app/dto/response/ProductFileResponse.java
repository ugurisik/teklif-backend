package com.teklif.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFileResponse {
    private String id;
    private String productId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String fileExtension;
    private String category;
    private Instant createdAt;
}
