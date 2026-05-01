package com.smartgig.notification.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionManager {
    private final Map<Long, String> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> reverse = new ConcurrentHashMap<>();

    public boolean isUserOnline(Long userId) {
        return userId != null && sessions.containsKey(userId);
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Long userId = parseUserId(accessor);
        if (sessionId != null && userId != null) {
            sessions.put(userId, sessionId);
            reverse.put(sessionId, userId);
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        if (sessionId == null) {
            return;
        }
        Long userId = reverse.remove(sessionId);
        if (userId != null) {
            sessions.remove(userId);
        }
    }

    private Long parseUserId(StompHeaderAccessor accessor) {
        return Optional.ofNullable(accessor.getFirstNativeHeader("X-User-Id"))
                .or(() -> Optional.ofNullable(accessor.getFirstNativeHeader("userId")))
                .filter(v -> !v.isBlank())
                .map(Long::parseLong)
                .orElse(null);
    }
}

