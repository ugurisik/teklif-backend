package com.teklif.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "offer_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OfferItem extends BaseEntity {

    @Column(nullable = false)
    private String offerId;

    private String productId;

    @Column(nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer vatRate;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal vatAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offerId", insertable = false, updatable = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;
}