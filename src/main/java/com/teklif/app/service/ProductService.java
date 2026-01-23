package com.teklif.app.service;

import com.teklif.app.dto.request.ProductRequest;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.dto.response.PaginationResponse;
import com.teklif.app.dto.response.ProductResponse;
import com.teklif.app.entity.Product;
import com.teklif.app.exception.CustomException;
import com.teklif.app.mapper.ProductMapper;
import com.teklif.app.repository.ProductRepository;
import com.teklif.app.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

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

        return productMapper.toResponse(product);
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
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request) {
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Product not found"));

        productMapper.updateEntity(request, product);
        product = productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Transactional
    public void deleteProduct(String id) {
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Product not found"));

        product.setIsDeleted(true);
        productRepository.save(product);
    }
}