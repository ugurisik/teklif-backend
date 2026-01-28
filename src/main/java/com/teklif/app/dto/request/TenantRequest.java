package com.teklif.app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TenantRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Slug is required")
    private String slug;

    private String logo;

    @NotBlank(message = "Tax number is required")
    private String taxNumber;

    private String taxOffice;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String address;

    // Theme settings
    private String primaryColor;
    private String secondaryColor;

    // Email settings
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUser;
    private String smtpPassword;
    private String fromEmail;
    private String fromName;

    // Package limits
    private String packageName;
    private Integer maxUsers;
    private Integer maxOffers;
    private Integer maxCustomers;

    private Boolean isActive;

    private String template;
}
