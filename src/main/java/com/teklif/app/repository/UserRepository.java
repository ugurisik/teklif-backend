package com.teklif.app.repository;

import com.teklif.app.entity.User;
import com.teklif.app.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByIdAndIsDeletedFalse(String id);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false " +
            "AND (:tenantId IS NULL OR u.tenantId = :tenantId) " +
            "AND (:search IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "    OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> findAllWithFilters(
            @Param("tenantId") String tenantId,
            @Param("search") String search,
            @Param("role") Role role,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    long countByTenantIdAndIsDeletedFalse(String tenantId);

    boolean existsByEmailAndIsDeletedFalse(String email);
}