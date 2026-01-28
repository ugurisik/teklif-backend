package com.teklif.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferBasicResponse {
    private String id;
    private String offerNo;
    private String customerId;
    private CustomerBasicResponse customer;
}
