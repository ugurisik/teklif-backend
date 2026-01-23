package com.teklif.app.entity;

import com.teklif.app.enums.CustomerType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Customer extends BaseEntity {

    @Column(nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType type;

    // For COMPANY type
    private String companyName;

    // For INDIVIDUAL type
    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private String contactPerson;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String taxNumber;
    private String taxOffice;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Offer> offers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenantId", insertable = false, updatable = false)
    private Tenant tenant;
}