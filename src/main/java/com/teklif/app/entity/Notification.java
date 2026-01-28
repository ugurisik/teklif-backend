package com.teklif.app.entity;

import com.teklif.app.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offerId", insertable = false, updatable = false)
    private Offer offer;

    @Column(name = "offerId")
    private String offerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenantId", insertable = false, updatable = false)
    private Tenant tenant;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NotificationRead> reads = new ArrayList<>();
}