package com.teklif.app.repository;

import com.teklif.app.entity.Offer;
import com.teklif.app.enums.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, String> {

    Optional<Offer> findByIdAndTenantIdAndIsDeletedFalse(String id, String tenantId);

    Optional<Offer> findByUuidAndIsDeletedFalse(String uuid);

    @Query("SELECT o FROM Offer o WHERE o.isDeleted = false " +
            "AND o.tenantId = :tenantId " +
            "AND (:search IS NULL OR LOWER(o.offerNo) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:customerId IS NULL OR o.customerId = :customerId) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate)")
    Page<Offer> findAllWithFilters(
            @Param("tenantId") String tenantId,
            @Param("search") String search,
            @Param("status") OfferStatus status,
            @Param("customerId") String customerId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    @Query("SELECT o FROM Offer o WHERE o.tenantId = :tenantId " +
            "AND o.isDeleted = false " +
            "ORDER BY o.createdAt DESC")
    Page<Offer> findRecentOffers(@Param("tenantId") String tenantId, Pageable pageable);

    long countByTenantIdAndStatusAndIsDeletedFalse(String tenantId, OfferStatus status);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Offer o WHERE o.tenantId = :tenantId " +
            "AND o.status = :status AND o.isDeleted = false")
    Double sumTotalByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") OfferStatus status);

    @Query("SELECT COUNT(o) FROM Offer o WHERE o.offerNo LIKE CONCAT(:prefix, '%') AND o.isDeleted = false")
    long countByOfferNoPrefix(@Param("prefix") String prefix);

    List<Offer> findByStatusAndValidUntilBeforeAndIsDeletedFalse(OfferStatus status, Instant validUntil);
}