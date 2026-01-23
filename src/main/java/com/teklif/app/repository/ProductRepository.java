package com.teklif.app.repository;

import com.teklif.app.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findByIdAndTenantIdAndIsDeletedFalse(String id, String tenantId);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false " +
            "AND p.tenantId = :tenantId " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "    OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:isActive IS NULL OR p.isActive = :isActive)")
    Page<Product> findAllWithFilters(
            @Param("tenantId") String tenantId,
            @Param("search") String search,
            @Param("category") String category,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    long countByTenantIdAndIsDeletedFalse(String tenantId);

    boolean existsByCodeAndTenantIdAndIsDeletedFalse(String code, String tenantId);
}