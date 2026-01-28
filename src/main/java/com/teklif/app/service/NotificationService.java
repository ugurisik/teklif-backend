package com.teklif.app.service;

import com.teklif.app.dto.request.NotificationRequest;
import com.teklif.app.dto.response.NotificationReadResponse;
import com.teklif.app.dto.response.NotificationResponse;
import com.teklif.app.dto.response.OfferBasicResponse;
import com.teklif.app.entity.Notification;
import com.teklif.app.entity.NotificationRead;
import com.teklif.app.enums.NotificationType;
import com.teklif.app.exception.CustomException;
import com.teklif.app.mapper.CustomerMapper;
import com.teklif.app.mapper.NotificationMapper;
import com.teklif.app.mapper.OfferMapper;
import com.teklif.app.repository.NotificationReadRepository;
import com.teklif.app.repository.NotificationRepository;
import com.teklif.app.repository.OfferRepository;
import com.teklif.app.repository.UserRepository;
import com.teklif.app.security.CustomUserDetails;
import com.teklif.app.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;

    public List<NotificationResponse> getTenantNotifications() {
        String tenantId = TenantContext.getTenantId();
        List<Notification> notifications = notificationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        String currentUserId = getCurrentUserId();

        return notifications.stream()
                .map(notification -> {
                    NotificationResponse response = notificationMapper.toResponse(notification);

                    // Set reads
                    List<NotificationReadResponse> readResponses = notification.getReads().stream()
                            .map(notificationMapper::toReadResponse)
                            .collect(Collectors.toList());
                    response.setReads(readResponses);

                    // Set isReadByCurrentUser
                    boolean isReadByCurrentUser = notification.getReads().stream()
                            .anyMatch(nr -> nr.getUserId().equals(currentUserId) && nr.getIsRead());
                    response.setIsReadByCurrentUser(isReadByCurrentUser);

                    return response;
                })
                .collect(Collectors.toList());
    }

    public NotificationResponse getNotificationById(String id) {
        String tenantId = TenantContext.getTenantId();
        List<Notification> notifications = notificationRepository.findByIdWithFetch(id);

        Notification notification = notifications.stream()
                .filter(n -> n.getTenantId().equals(tenantId))
                .findFirst()
                .orElseThrow(() -> CustomException.notFound("Notification not found"));

        NotificationResponse response = notificationMapper.toResponse(notification);
        String currentUserId = getCurrentUserId();

        // Set reads
        List<NotificationReadResponse> readResponses = notification.getReads().stream()
                .map(notificationMapper::toReadResponse)
                .collect(Collectors.toList());
        response.setReads(readResponses);

        // Set isReadByCurrentUser
        boolean isReadByCurrentUser = notification.getReads().stream()
                .anyMatch(nr -> nr.getUserId().equals(currentUserId) && nr.getIsRead());
        response.setIsReadByCurrentUser(isReadByCurrentUser);

        return response;
    }

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        String tenantId = TenantContext.getTenantId();

        // Validate offer if provided
        if (request.getOfferId() != null) {
            offerRepository.findByIdAndTenantIdAndIsDeletedFalse(request.getOfferId(), tenantId)
                    .orElseThrow(() -> CustomException.notFound("Offer not found"));
        }

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .tenantId(tenantId)
                .offerId(request.getOfferId())
                .build();

        notification = notificationRepository.save(notification);

        // Create notification reads for all tenant users
        List<NotificationRead> reads = createNotificationReadsForTenantUsers(notification.getId(), tenantId);
        notification.setReads(reads);

        return buildNotificationResponse(notification, getCurrentUserId());
    }

    @Transactional
    public void markAsRead(String notificationId) {
        String userId = getCurrentUserId();

        NotificationRead notificationRead = notificationReadRepository
                .findByNotificationAndUser(notificationId, userId)
                .orElse(null);

        if (notificationRead != null && !notificationRead.getIsRead()) {
            notificationRead.setIsRead(true);
            notificationRead.setReadAt(Instant.now());
            notificationReadRepository.save(notificationRead);
        }
    }

    @Transactional
    public void markAllAsRead() {
        String userId = getCurrentUserId();
        List<NotificationRead> unreadNotifications = notificationReadRepository
                .findByUserIdAndIsDeletedFalse(userId)
                .stream()
                .filter(nr -> !nr.getIsRead())
                .collect(Collectors.toList());

        for (NotificationRead nr : unreadNotifications) {
            nr.setIsRead(true);
            nr.setReadAt(Instant.now());
        }

        notificationReadRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public void deleteNotification(String id) {
        String tenantId = TenantContext.getTenantId();
        List<Notification> notifications = notificationRepository.findByIdWithFetch(id);

        Notification notification = notifications.stream()
                .filter(n -> n.getTenantId().equals(tenantId))
                .findFirst()
                .orElseThrow(() -> CustomException.notFound("Notification not found"));

        notification.setIsDeleted(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotificationForOffer(String offerId, String tenantId, NotificationType type, String title, String message) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .tenantId(tenantId)
                .offerId(offerId)
                .build();

        notification = notificationRepository.save(notification);

        // Create notification reads for all tenant users
        createNotificationReadsForTenantUsers(notification.getId(), tenantId);
    }

    // Helper methods

    private List<NotificationRead> createNotificationReadsForTenantUsers(String notificationId, String tenantId) {
        List<NotificationRead> reads = new ArrayList<>();

        // Get all active users for this tenant
        userRepository.findByTenantIdAndIsActiveAndIsDeletedFalse(tenantId, true)
                .forEach(user -> {
                    NotificationRead read = NotificationRead.builder()
                            .notificationId(notificationId)
                            .userId(user.getId())
                            .isRead(false)
                            .build();
                    reads.add(notificationReadRepository.save(read));
                });

        return reads;
    }

    private NotificationResponse buildNotificationResponse(Notification notification, String currentUserId) {
        NotificationResponse response = notificationMapper.toResponse(notification);

        List<NotificationReadResponse> readResponses = notification.getReads().stream()
                .map(notificationMapper::toReadResponse)
                .collect(Collectors.toList());
        response.setReads(readResponses);

        boolean isReadByCurrentUser = notification.getReads().stream()
                .anyMatch(nr -> nr.getUserId().equals(currentUserId) && nr.getIsRead());
        response.setIsReadByCurrentUser(isReadByCurrentUser);

        return response;
    }

    private String getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getUser().getId();
    }
}
