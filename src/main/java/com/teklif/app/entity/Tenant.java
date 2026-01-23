package com.teklif.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Tenant extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String logo;

    @Column(nullable = false)
    private String taxNumber;

    private String taxOffice;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(columnDefinition = "TEXT")
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

    @Column(nullable = false)
    private Boolean isActive = true;
}