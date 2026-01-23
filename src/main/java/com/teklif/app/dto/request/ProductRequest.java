package com.teklif.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Product code is required")
    private String code;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "VAT rate is required")
    private Integer vatRate;

    @NotBlank(message = "Unit is required")
    private String unit;

    private String category;
}