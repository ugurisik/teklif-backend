package com.teklif.app.controller;

import com.teklif.app.dto.request.ProductRequest;
import com.teklif.app.dto.response.ApiResponse;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.dto.response.ProductFileResponse;
import com.teklif.app.dto.response.ProductResponse;
import com.teklif.app.entity.ProductFile;
import com.teklif.app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        PagedResponse<ProductResponse> response = productService.getAllProducts(search, category, isActive, page, limit);
        return ResponseEntity.ok(ApiResponse.success(response, response.getPagination()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create new product")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.successWithMessage("Product deleted successfully"));
    }

    // File endpoints

    @GetMapping("/{id}/files")
    @Operation(summary = "Get product files")
    public ResponseEntity<ApiResponse<List<ProductFileResponse>>> getProductFiles(@PathVariable String id) {
        List<ProductFileResponse> response = productService.getProductFiles(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/files")
    @Operation(summary = "Add file to product")
    public ResponseEntity<ApiResponse<ProductFileResponse>> addFile(
            @PathVariable String id,
            @RequestBody AddFileRequest request
    ) {
        ProductFileResponse response = productService.addFile(
                id,
                request.getFileName(),
                request.getFilePath(),
                request.getFileSize(),
                request.getFileType(),
                request.getFileExtension(),
                request.getCategory()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/files/{fileId}")
    @Operation(summary = "Remove file from product")
    public ResponseEntity<ApiResponse<Void>> removeFile(@PathVariable String fileId) {
        productService.removeFile(fileId);
        return ResponseEntity.ok(ApiResponse.successWithMessage("File removed successfully"));
    }

    @Data
    static class AddFileRequest {
        private String fileName;
        private String filePath;
        private Long fileSize;
        private String fileType;
        private String fileExtension;
        private ProductFile.FileType category;
    }
}