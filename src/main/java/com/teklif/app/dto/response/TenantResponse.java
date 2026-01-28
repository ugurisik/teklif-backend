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
public class TenantResponse {
    private String id;
    private String name;
    private String slug;
    private String logo;
    private String taxNumber;
    private String taxOffice;
    private String email;
    private String phone;
    private String address;
    private String primaryColor;
    private String secondaryColor;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUser;
    private String fromEmail;
    private String fromName;
    private String packageName;
    private Integer maxUsers;
    private Integer maxOffers;
    private Integer maxCustomers;
    private Boolean isActive;
    private String template;
    private Instant createdAt;
    private Instant updatedAt;
}
