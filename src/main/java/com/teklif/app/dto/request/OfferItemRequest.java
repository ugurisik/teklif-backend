package com.teklif.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OfferItemRequest {

    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    private String description;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;

    @NotNull(message = "VAT rate is required")
    private Integer vatRate;

    private BigDecimal discountRate = BigDecimal.ZERO;
}