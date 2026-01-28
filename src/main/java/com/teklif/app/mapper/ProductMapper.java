package com.teklif.app.mapper;

import com.teklif.app.dto.request.ProductRequest;
import com.teklif.app.dto.response.ProductFileResponse;
import com.teklif.app.dto.response.ProductResponse;
import com.teklif.app.entity.Product;
import com.teklif.app.entity.ProductFile;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "files", ignore = true)
    ProductResponse toResponse(Product product);

    default List<ProductFileResponse> mapFiles(List<ProductFile> files) {
        if (files == null) {
            return null;
        }
        return files.stream()
                .map(this::toFileResponse)
                .collect(Collectors.toList());
    }

    default ProductFileResponse toFileResponse(ProductFile file) {
        if (file == null) {
            return null;
        }
        return ProductFileResponse.builder()
                .id(file.getId())
                .productId(file.getProductId())
                .fileName(file.getFileName())
                .filePath(file.getFilePath())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .fileExtension(file.getFileExtension())
                .category(file.getCategory() != null ? file.getCategory().name() : null)
                .createdAt(file.getCreatedAt())
                .build();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Product toEntity(ProductRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}