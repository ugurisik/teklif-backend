package com.teklif.app.repository;

import com.teklif.app.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND n.isDeleted = false " +
            "AND (:isRead IS NULL OR n.isRead = :isRead) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdWithFilters(
            @Param("userId") String userId,
            @Param("isRead") Boolean isRead,
            Pageable pageable
    );

    long countByUserIdAndIsReadFalseAndIsDeletedFalse(String userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isDeleted = false")
    void markAllAsReadByUserId(@Param("userId") String userId);
}