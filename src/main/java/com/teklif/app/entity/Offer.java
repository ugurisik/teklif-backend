package com.teklif.app.entity;

import com.teklif.app.enums.OfferStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Offer extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String offerNo;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false, unique = true)
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OfferStatus status = OfferStatus.DRAFT;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal vatTotal;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    @Column(nullable = false, length = 3)
    private String currency;

    private Instant validUntil;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Link settings
    private String linkPassword;

    @Column(nullable = false)
    @Builder.Default
    private Boolean oneTimeView = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean hasBeenViewed = false;

    // Activity tracking
    private Instant sentAt;
    private Instant viewedAt;
    private Instant acceptedAt;
    private Instant rejectedAt;

    private String viewedBy;
    private String acceptedBy;
    private String rejectedBy;
    private String rejectionNote;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OfferItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenantId", insertable = false, updatable = false)
    private Tenant tenant;

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }
}