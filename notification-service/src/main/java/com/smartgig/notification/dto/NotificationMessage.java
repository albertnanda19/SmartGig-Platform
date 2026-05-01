package com.smartgig.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private Long userId;
    private String type;
    private String title;
    private String message;
    private String actionUrl;
    private Map<String, Object> metadata;
}

