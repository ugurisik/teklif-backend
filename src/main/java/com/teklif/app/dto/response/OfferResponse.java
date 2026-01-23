package com.teklif.app.dto.response;

import com.teklif.app.enums.OfferStatus;
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
public class OfferResponse {
    private String id;
    private String offerNo;
    private String tenantId;
    private String customerId;
    private CustomerBasicResponse customer;
    private String uuid;
    private OfferStatus status;
    private BigDecimal subtotal;
    private BigDecimal vatTotal;
    private BigDecimal total;
    private String currency;
    private Instant validUntil;
    private String notes;
    private Boolean oneTimeView;
    private Boolean hasBeenViewed;
    private Instant sentAt;
    private Instant viewedAt;
    private String viewedBy;
    private Instant acceptedAt;
    private String acceptedBy;
    private Instant rejectedAt;
    private String rejectedBy;
    private String rejectionNote;
    private List<OfferItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;
}