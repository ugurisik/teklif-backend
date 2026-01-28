package com.teklif.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String tenantId;
    private String code;
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private String currency;
    private Integer vatRate;
    private String unit;
    private String category;
    private Boolean isActive;
    private List<ProductFileResponse> files;
    private Instant createdAt;
    private Instant updatedAt;
}