package com.teklif.app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserTenantResponse {
    private String id;
    private String tenantId;
    private String name;
    private String slug;
    private String parentTenantId; // null ise root tenant
    private Boolean isDefault;
    private Instant createdAt;
}
