package com.teklif.app.repository;

import com.teklif.app.entity.ActivityLog;
import com.teklif.app.enums.LogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {

    @Query("SELECT al FROM ActivityLog al " +
            "LEFT JOIN FETCH al.user " +
            "LEFT JOIN FETCH al.tenant " +
            "WHERE al.tenantId = :tenantId AND al.isDeleted = false " +
            "ORDER BY al.logDate DESC")
    List<ActivityLog> findByTenantIdOrderByLogDateDesc(@Param("tenantId") String tenantId);

    @Query("SELECT al FROM ActivityLog al " +
            "LEFT JOIN FETCH al.user " +
            "LEFT JOIN FETCH al.tenant " +
            "WHERE al.tenantId = :tenantId AND al.isDeleted = false " +
            "AND (:logType IS NULL OR al.logType = :logType) " +
            "AND (:targetId IS NULL OR al.targetId = :targetId) " +
            "AND (:startDate IS NULL OR al.logDate >= :startDate) " +
            "AND (:endDate IS NULL OR al.logDate <= :endDate) " +
            "ORDER BY al.logDate DESC")
    Page<ActivityLog> findByTenantIdWithFilters(
            @Param("tenantId") String tenantId,
            @Param("logType") LogType logType,
            @Param("targetId") String targetId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    @Query("SELECT al FROM ActivityLog al " +
            "WHERE al.targetId = :targetId AND al.isDeleted = false " +
            "ORDER BY al.logDate DESC")
    List<ActivityLog> findByTargetIdOrderByLogDateDesc(@Param("targetId") String targetId);

    @Query("SELECT al FROM ActivityLog al " +
            "WHERE al.userId = :userId AND al.isDeleted = false " +
            "ORDER BY al.logDate DESC")
    List<ActivityLog> findByUserIdOrderByLogDateDesc(@Param("userId") String userId);

    @Query("SELECT COUNT(al) FROM ActivityLog al " +
            "WHERE al.tenantId = :tenantId AND al.isDeleted = false")
    long countByTenantId(@Param("tenantId") String tenantId);
}
