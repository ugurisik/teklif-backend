package com.teklif.app.entity;

import com.teklif.app.enums.LogType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_log_type", columnList = "logType"),
    @Index(name = "idx_target_id", columnList = "targetId"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ActivityLog extends BaseEntity {

    @Column(nullable = false)
    private Instant logDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogType logType;

    @Column(nullable = false, length = 100)
    private String targetId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 100)
    private String osName;

    @Column(length = 100)
    private String osVersion;

    @Column(length = 100)
    private String browserName;

    @Column(length = 100)
    private String browserVersion;

    @Column(length = 50)
    private String deviceType;

    @Column(nullable = false)
    private String tenantId;

    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenantId", insertable = false, updatable = false)
    private Tenant tenant;

    @PrePersist
    public void prePersist() {
        if (logDate == null) {
            logDate = Instant.now();
        }

        if (getCreatedAt() == null) {
            setCreatedAt(Instant.now());
        }
        if (getUpdatedAt() == null) {
            setUpdatedAt(Instant.now());
        }
        if (getIsDeleted() == null) {
            setIsDeleted(false);
        }
    }

    @PreUpdate
    public void preUpdate() {
        setUpdatedAt(Instant.now());
    }
}
