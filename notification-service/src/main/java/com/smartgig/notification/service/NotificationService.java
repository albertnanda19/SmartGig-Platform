package com.smartgig.notification.service;

import com.smartgig.notification.document.NotificationDocument;
import com.smartgig.notification.dto.NotificationMessage;
import com.smartgig.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    NotificationDocument createAndSendNotification(NotificationMessage message);

    Page<NotificationResponse> getNotificationsForUser(Long userId, Pageable pageable);

    void markAsRead(Long userId, String notificationId);

    void markAllAsRead(Long userId);

    long getUnreadCount(Long userId);

    boolean isEventProcessed(String eventId);

    void markEventProcessed(String eventId);
}

