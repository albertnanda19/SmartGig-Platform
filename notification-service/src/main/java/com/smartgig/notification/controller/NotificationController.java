package com.smartgig.notification.controller;

import com.smartgig.common.constants.AppConstants;
import com.smartgig.common.response.ApiResponse;
import com.smartgig.notification.dto.response.NotificationResponse;
import com.smartgig.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification inbox")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get my notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            HttpServletRequest http,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = extractUserId(http);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotificationsForUser(userId, pageable)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(HttpServletRequest http) {
        Long userId = extractUserId(http);
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(userId)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(HttpServletRequest http, @PathVariable String id) {
        Long userId = extractUserId(http);
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(HttpServletRequest http) {
        Long userId = extractUserId(http);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Long extractUserId(HttpServletRequest request) {
        String raw = request.getHeader(AppConstants.USER_ID_HEADER);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Long.parseLong(raw);
    }
}

