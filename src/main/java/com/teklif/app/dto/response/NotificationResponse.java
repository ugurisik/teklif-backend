package com.teklif.app.dto.response;

import com.teklif.app.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String title;
    private String message;
    private NotificationType type;
    private String tenantId;
    private String offerId;
    private OfferBasicResponse offer;
    private CompanyBasicResponse tenant;
    private List<NotificationReadResponse> reads;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isReadByCurrentUser; // Computed field for current user
}
