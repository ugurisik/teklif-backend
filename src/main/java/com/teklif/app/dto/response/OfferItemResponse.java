package com.teklif.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferItemResponse {
    private String id;
    private String productId;
    private String productName;
    private String description;
    private Integer quantity;
    private String unit;
    private BigDecimal unitPrice;
    private Integer vatRate;
    private BigDecimal discountRate;
    private BigDecimal subtotal;
    private BigDecimal vatAmount;
    private BigDecimal total;
    private List<ProductFileResponse> files;
}