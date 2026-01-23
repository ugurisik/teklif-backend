package com.teklif.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CreateOfferRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Currency is required")
    private String currency;

    private Instant validUntil;

    private String notes;

    private OfferLinkSettings linkSettings;

    @NotEmpty(message = "At least one item is required")
    private List<OfferItemRequest> items;
}