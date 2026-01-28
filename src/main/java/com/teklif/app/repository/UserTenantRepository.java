package com.teklif.app.repository;

import com.teklif.app.entity.UserTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTenantRepository extends JpaRepository<UserTenant, String> {

    @Query("SELECT ut FROM UserTenant ut " +
            "LEFT JOIN FETCH ut.tenant " +
            "WHERE ut.userId = :userId AND ut.isDeleted = false " +
            "AND ut.tenant.isDeleted = false")
    List<UserTenant> findByUserIdAndIsDeletedFalse(@Param("userId") String userId);

    @Query("SELECT ut FROM UserTenant ut " +
            "LEFT JOIN FETCH ut.tenant " +
            "WHERE ut.userId = :userId AND ut.isDefault = true AND ut.isDeleted = false")
    Optional<UserTenant> findDefaultByUserId(@Param("userId") String userId);

    @Query("SELECT CASE WHEN COUNT(ut) > 0 THEN true ELSE false END " +
            "FROM UserTenant ut " +
            "WHERE ut.userId = :userId AND ut.tenantId = :tenantId AND ut.isDeleted = false")
    boolean existsByUserIdAndTenantId(@Param("userId") String userId, @Param("tenantId") String tenantId);

    @Query("SELECT ut FROM UserTenant ut " +
            "WHERE ut.userId = :userId AND ut.tenantId = :tenantId AND ut.isDeleted = false")
    Optional<UserTenant> findByUserIdAndTenantId(@Param("userId") String userId, @Param("tenantId") String tenantId);
}
