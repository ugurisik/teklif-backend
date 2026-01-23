package com.teklif.app.repository;

import com.teklif.app.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    Optional<Tenant> findByIdAndIsDeletedFalse(String id);

    Optional<Tenant> findBySlugAndIsDeletedFalse(String slug);

    @Query("SELECT t FROM Tenant t WHERE t.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "    OR LOWER(t.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:isActive IS NULL OR t.isActive = :isActive)")
    Page<Tenant> findAllWithFilters(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    boolean existsBySlugAndIsDeletedFalse(String slug);
}