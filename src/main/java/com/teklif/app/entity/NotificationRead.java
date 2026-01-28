package com.teklif.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "notification_reads", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"notificationId", "userId"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NotificationRead extends BaseEntity {

    @Column(nullable = false)
    private String notificationId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    private Instant readAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificationId", insertable = false, updatable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        if (getCreatedAt() == null) {
            setCreatedAt(Instant.now());
        }
        if (getUpdatedAt() == null) {
            setUpdatedAt(Instant.now());
        }
        if (getIsDeleted() == null) {
            setIsDeleted(false);
        }
        if (isRead == null) {
            isRead = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        setUpdatedAt(Instant.now());
    }
}
