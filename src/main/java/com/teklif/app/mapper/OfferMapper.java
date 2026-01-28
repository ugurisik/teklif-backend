package com.teklif.app.mapper;

import com.teklif.app.dto.request.CreateOfferRequest;
import com.teklif.app.dto.response.OfferBasicResponse;
import com.teklif.app.dto.response.OfferItemResponse;
import com.teklif.app.dto.response.OfferResponse;
import com.teklif.app.dto.response.ProductFileResponse;
import com.teklif.app.entity.Offer;
import com.teklif.app.entity.OfferItem;
import com.teklif.app.entity.ProductFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class, TenantMapper.class, ProductMapper.class})
public interface OfferMapper {

    default OfferResponse toResponse(Offer offer) {
        if (offer == null) {
            return null;
        }

        OfferResponse response = OfferResponse.builder()
                .id(offer.getId())
                .offerNo(offer.getOfferNo())
                .customerId(offer.getCustomerId())
                .customer(offer.getCustomer() != null ?
                        getCustomerMapper().toBasicResponse(offer.getCustomer()) : null)
                .company(offer.getTenant() != null ?
                        getTenantMapper().toCompanyBasicResponse(offer.getTenant()) : null)
                .items(offer.getItems().stream()
                        .filter(item -> !item.getIsDeleted())
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()))
                .subtotal(offer.getSubtotal())
                .vatTotal(offer.getVatTotal())
                .total(offer.getTotal())
                .currency(offer.getCurrency())
                .validUntil(offer.getValidUntil())
                .notes(offer.getNotes())
                .status(offer.getStatus())

                .passwordRequired(offer.getLinkPassword() == null ? false : true)
                .oneTimeView(offer.getOneTimeView())
                .hasBeenViewed(offer.getHasBeenViewed())
                .showTlEquivalent(offer.getShowTlEquivalent())
                .showExchangeRateInfo(offer.getShowExchangeRateInfo())
                .sentAt(offer.getSentAt())
                .exchangeRate(offer.getExchangeRate())
                .viewedAt(offer.getViewedAt())
                .viewedBy(offer.getViewedBy())
                .acceptedAt(offer.getAcceptedAt())
                .uuid(offer.getUuid())
                .acceptedBy(offer.getAcceptedBy())
                .tenantId(offer.getTenantId())
                .rejectedAt(offer.getRejectedAt())
                .rejectedBy(offer.getRejectedBy())
                .rejectionNote(offer.getRejectionNote())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .build();

        return response;
    }

    private CustomerMapper getCustomerMapper() {
        return new com.teklif.app.mapper.CustomerMapperImpl();
    }

    private TenantMapper getTenantMapper() {
        return new com.teklif.app.mapper.TenantMapperImpl();
    }

    default OfferItemResponse toItemResponse(OfferItem item) {
        if (item == null) {
            return null;
        }

        OfferItemResponse response = OfferItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .unitPrice(item.getUnitPrice())
                .vatRate(item.getVatRate())
                .discountRate(item.getDiscountRate())
                .subtotal(item.getSubtotal())
                .vatAmount(item.getVatAmount())
                .total(item.getTotal())
                .build();

        // Map product files if product exists
        try {
            if (item.getProduct() != null && item.getProduct().getFiles() != null) {
                List<ProductFileResponse> files = item.getProduct().getFiles().stream()
                        .filter(f -> !f.getIsDeleted())
                        .map(this::toFileResponse)
                        .collect(Collectors.toList());
                response.setFiles(files);
            }
        } catch (Exception e) {
            // Lazy loading failed, ignore files
        }

        return response;
    }

    default ProductFileResponse toFileResponse(ProductFile file) {
        if (file == null) {
            return null;
        }
        if(file.getFileExtension() == null){
            file.setFileExtension(Arrays.stream(file.getFilePath().split("\\.")).toList().getLast());
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

    default OfferBasicResponse toBasicResponse(Offer offer) {
        if (offer == null) {
            return null;
        }
        return OfferBasicResponse.builder()
                .id(offer.getId())
                .offerNo(offer.getOfferNo())
                .customerId(offer.getCustomerId())
                .build();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "offerNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "vatTotal", ignore = true)
    @Mapping(target = "total", ignore = true)
    void updateEntity(CreateOfferRequest request, @MappingTarget Offer offer);

}