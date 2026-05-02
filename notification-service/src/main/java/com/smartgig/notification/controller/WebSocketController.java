package com.smartgig.notification.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WebSocket", description = "WebSocket endpoints")
public class WebSocketController {

    @MessageMapping("/ping")
    public void ping(@Payload Map<String, Object> payload) {
    }
}

