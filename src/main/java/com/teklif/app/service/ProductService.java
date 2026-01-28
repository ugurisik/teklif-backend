package com.teklif.app.service;

import com.teklif.app.dto.request.ProductRequest;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.dto.response.PaginationResponse;
import com.teklif.app.dto.response.ProductFileResponse;
import com.teklif.app.dto.response.ProductResponse;
import com.teklif.app.entity.Product;
import com.teklif.app.entity.ProductFile;
import com.teklif.app.enums.LogType;
import com.teklif.app.exception.CustomException;
import com.teklif.app.mapper.ProductMapper;
import com.teklif.app.repository.ProductFileRepository;
import com.teklif.app.repository.ProductRepository;
import com.teklif.app.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductFileRepository productFileRepository;
    private final ActivityLogService activityLogService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public PagedResponse<ProductResponse> getAllProducts(
            String search,
            String category,
            Boolean isActive,
            int page,
            int limit
    ) {
        String tenantId = TenantContext.getTenantId();
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Page<Product> productPage = productRepository.findAllWithFilters(
                tenantId, search, category, isActive, pageable
        );

        List<ProductResponse> items = productPage.getContent().stream()
                .map(productMapper::toResponse)
                .toList();

        PaginationResponse pagination = PaginationResponse.of(
                productPage.getTotalElements(), page, limit
        );

        return PagedResponse.<ProductResponse>builder()
                .items(items)
                .pagination(pagination)
                .build();
    }

    public ProductResponse getProductById(String id) {
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Product not found"));

        // Load files
        List<ProductFile> files = productFileRepository.findByProductIdWithFetch(id);

        ProductResponse response = productMapper.toResponse(product);
        response.setFiles(productMapper.mapFiles(files));

        return response;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String tenantId = TenantContext.getTenantId();

        // Check if code exists
        if (productRepository.existsByCodeAndTenantIdAndIsDeletedFalse(request.getCode(), tenantId)) {
            throw CustomException.badRequest("Product code already exists");
        }

        Product product = productMapper.toEntity(request);
        product.setTenantId(tenantId);

        product = productRepository.save(product);

        // Create log
        activityLogService.createLog(LogType.PRODUCT_CREATED, product.getId(),
                "Ürün Oluşturuldu",
                product.getCode() + " kodlu ürün oluşturuldu. " + product.getName(),
                null);

        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request) {
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Product not found"));

        productMapper.updateEntity(request, product);
        product = productRepository.save(product);

        // Create log
        activityLogService.createLog(LogType.PRODUCT_UPDATED, product.getId(),
                "Ürün Güncellendi",
                product.getCode() + " kodlu ürün güncellendi. " + product.getName(),
                null);

        return productMapper.toResponse(product);
    }

    @Transactional
    public void deleteProduct(String id) {
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Product not found"));

        String productCode = product.getCode();
        String productName = product.getName();

        product.setIsDeleted(true);
        productRepository.save(product);

        // Create log
        activityLogService.createLog(LogType.PRODUCT_DELETED, id,
                "Ürün Silindi",
                productCode + " kodlu ürün silindi. " + productName,
                null);
    }

    @Transactional
    public ProductFileResponse addFile(String productId, String fileName, String filePath, Long fileSize, String fileType, String fileExtension, ProductFile.FileType category) {
        String tenantId = TenantContext.getTenantId();

        // Validate product
        Product product = productRepository.findByIdAndTenantIdAndIsDeletedFalse(productId, tenantId)
                .orElseThrow(() -> CustomException.notFound("Product not found"));

        ProductFile productFile = ProductFile.builder()
                .productId(productId)
                .tenantId(tenantId)
                .fileName(fileName)
                .filePath(filePath)
                .fileSize(fileSize)
                .fileType(fileType)
                .fileExtension(fileExtension)
                .category(category)
                .build();

        productFile = productFileRepository.save(productFile);

        // Create log
        activityLogService.createLog(LogType.PRODUCT_UPDATED, productId,
                "Ürün Görseli Eklendi",
                product.getCode() + " kodlu ürüne " + category.name() + " türünde görsel eklendi: " + fileName,
                null);

        return productMapper.toFileResponse(productFile);
    }

    @Transactional
    public void removeFile(String fileId) {
        String tenantId = TenantContext.getTenantId();

        ProductFile productFile = productFileRepository.findById(fileId)
                .orElseThrow(() -> CustomException.notFound("File not found"));

        if (!productFile.getTenantId().equals(tenantId)) {
            throw CustomException.forbidden("Access denied");
        }

        String productId = productFile.getProductId();
        String fileName = productFile.getFileName();
        String category = productFile.getCategory() != null ? productFile.getCategory().name() : "DOSYA";

        // Get product info for log
        Product product = productRepository.findByIdAndTenantIdAndIsDeletedFalse(productId, tenantId)
                .orElseThrow(() -> CustomException.notFound("Product not found"));

        // Delete physical file
        try {
            String filePath = productFile.getFilePath();
            java.nio.file.Path fullPath = Paths.get(uploadDir, filePath);

            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
            }
        } catch (IOException e) {
            // Log error but continue with database deletion
            System.err.println("Failed to delete physical file: " + e.getMessage());
        }

        // Delete database record
        productFile.setIsDeleted(true);
        productFileRepository.save(productFile);

        // Create log
        activityLogService.createLog(LogType.PRODUCT_UPDATED, productId,
                "Ürün Görseli Silindi",
                product.getCode() + " kodlu üründen " + category + " türünde görsel silindi: " + fileName,
                null);
    }

    public List<ProductFileResponse> getProductFiles(String productId) {
        String tenantId = TenantContext.getTenantId();

        // Validate product
        productRepository.findByIdAndTenantIdAndIsDeletedFalse(productId, tenantId)
                .orElseThrow(() -> CustomException.notFound("Product not found"));

        List<ProductFile> files = productFileRepository.findByProductIdWithFetch(productId);

        return productMapper.mapFiles(files);
    }
}