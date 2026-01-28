package com.teklif.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "product_files", indexes = {
    @Index(name = "idx_product_id", columnList = "productId"),
    @Index(name = "idx_tenant_id", columnList = "tenantId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductFile extends BaseEntity {

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 50)
    private String fileType;

    @Column(length = 20)
    private String fileExtension;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileType category;

    public enum FileType {
        IMAGE,
        VIDEO,
        DOCUMENT,
        OTHER
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenantId", insertable = false, updatable = false)
    private Tenant tenant;

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
    }

    @PreUpdate
    public void preUpdate() {
        setUpdatedAt(Instant.now());
    }
}
