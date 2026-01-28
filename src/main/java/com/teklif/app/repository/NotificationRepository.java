package com.teklif.app.repository;

import com.teklif.app.entity.Notification;
import com.teklif.app.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.offer " +
            "LEFT JOIN FETCH n.tenant " +
            "WHERE n.id = :id AND n.isDeleted = false")
    List<Notification> findByIdWithFetch(@Param("id") String id);

    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.offer " +
            "LEFT JOIN FETCH n.tenant " +
            "WHERE n.tenantId = :tenantId AND n.isDeleted = false " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByTenantIdOrderByCreatedAtDesc(@Param("tenantId") String tenantId);

    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.offer " +
            "LEFT JOIN FETCH n.tenant " +
            "WHERE n.tenantId = :tenantId AND n.type = :type AND n.isDeleted = false " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByTenantIdAndType(@Param("tenantId") String tenantId, @Param("type") NotificationType type);

    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.offer " +
            "LEFT JOIN FETCH n.tenant " +
            "WHERE n.offerId = :offerId AND n.isDeleted = false " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByOfferId(@Param("offerId") String offerId);

    @Query("SELECT n FROM Notification n " +
            "WHERE n.tenantId = :tenantId AND n.isDeleted = false " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByTenantIdPaged(@Param("tenantId") String tenantId, Pageable pageable);
}
