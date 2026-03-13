package com.niu.community.message.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niu.community.common.web.JwtUtils;
import com.niu.community.message.dto.SendMessageDTO;
import com.niu.community.message.service.MessageService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final Map<Long, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();

    public MessageWebSocketHandler(JwtUtils jwtUtils, ObjectMapper objectMapper, MessageService messageService) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
        this.messageService = messageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session);
        if (userId == null) {
            session.close();
            return;
        }
        onlineUsers.put(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = extractUserId(session);
        if (userId == null) {
            return;
        }
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String type = payload.path("type").asText();
        if ("SEND".equalsIgnoreCase(type)) {
            SendMessageDTO dto = objectMapper.treeToValue(payload.path("data"), SendMessageDTO.class);
            var saved = messageService.sendMessage(userId, dto);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(new WebSocketMessage("SEND_SUCCESS", saved))));
            WebSocketSession target = onlineUsers.get(dto.getToUserId());
            if (target != null && target.isOpen()) {
                target.sendMessage(new TextMessage(objectMapper.writeValueAsString(new WebSocketMessage("NEW_MESSAGE", saved))));
            }
        } else if ("READ".equalsIgnoreCase(type)) {
            Long conversationId = payload.path("data").asLong();
            messageService.markAsRead(conversationId, userId);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(new WebSocketMessage("READ_SUCCESS", conversationId))));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        onlineUsers.values().remove(session);
    }

    private Long extractUserId(WebSocketSession session) {
        String query = session.getUri() == null ? null : session.getUri().getQuery();
        if (query == null || !query.contains("token=")) {
            return null;
        }
        String token = query.substring(query.indexOf("token=") + 6);
        return jwtUtils.getValidUserId(token);
    }
}
