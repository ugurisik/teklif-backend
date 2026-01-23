package com.teklif.app.repository;

import com.teklif.app.entity.Customer;
import com.teklif.app.enums.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByIdAndTenantIdAndIsDeletedFalse(String id, String tenantId);

    @Query("SELECT c FROM Customer c WHERE c.isDeleted = false " +
            "AND c.tenantId = :tenantId " +
            "AND (:search IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "    OR LOWER(c.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "    OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:type IS NULL OR c.type = :type) " +
            "AND (:isActive IS NULL OR c.isActive = :isActive)")
    Page<Customer> findAllWithFilters(
            @Param("tenantId") String tenantId,
            @Param("search") String search,
            @Param("type") CustomerType type,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    long countByTenantIdAndIsDeletedFalse(String tenantId);
}