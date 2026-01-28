package com.teklif.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

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

    private Boolean isActive = true;

    @Builder.Default
    private String template = "default";

    // Sub-tenant relationship (one-level hierarchy)
    @Column(name = "parent_tenant_id")
    private String parentTenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_tenant_id", insertable = false, updatable = false)
    private Tenant parentTenant;

    @OneToMany(mappedBy = "parentTenant", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Tenant> subTenants = new ArrayList<>();
}