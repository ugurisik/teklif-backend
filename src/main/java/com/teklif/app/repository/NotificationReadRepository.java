package com.teklif.app.repository;

import com.teklif.app.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, String> {

    Optional<NotificationRead> findByNotificationIdAndUserIdAndIsDeletedFalse(
            String notificationId, String userId
    );

    List<NotificationRead> findByNotificationIdAndIsDeletedFalse(String notificationId);

    List<NotificationRead> findByUserIdAndIsDeletedFalse(String userId);

    @Query("SELECT nr FROM NotificationRead nr " +
            "WHERE nr.notificationId = :notificationId AND nr.userId = :userId AND nr.isDeleted = false")
    Optional<NotificationRead> findByNotificationAndUser(
            @Param("notificationId") String notificationId,
            @Param("userId") String userId
    );

    @Query("SELECT COUNT(nr) FROM NotificationRead nr " +
            "WHERE nr.notificationId = :notificationId AND nr.isRead = true AND nr.isDeleted = false")
    Long countReadsByNotificationId(@Param("notificationId") String notificationId);

    @Query("SELECT COUNT(nr) FROM NotificationRead nr " +
            "WHERE nr.notificationId = :notificationId AND nr.isDeleted = false")
    Long totalReadsByNotificationId(@Param("notificationId") String notificationId);
}
