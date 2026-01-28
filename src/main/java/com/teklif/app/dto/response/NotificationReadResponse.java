package com.teklif.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReadResponse {
    private String id;
    private String notificationId;
    private String userId;
    private Boolean isRead;
    private Instant readAt;
    private Instant createdAt;
}
