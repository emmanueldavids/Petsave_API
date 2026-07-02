package com.petsave.petsave.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsave.petsave.Utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time notifications and chat delivery.
 * Sessions are keyed by user email (extracted from the JWT passed as a
 * `?token=` query param on connect) so messages can be routed to a specific
 * user, not just broadcast to every connection.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionIdToEmail = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String email = extractEmail(session);
        if (email == null) {
            log.warn("WebSocket connection rejected (missing/invalid token): {}", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Missing or invalid token"));
            return;
        }

        userSessions.put(email, session);
        sessionIdToEmail.put(session.getId(), email);
        log.info("WebSocket connection established for {}: {}", email, session.getId());

        sendMessage(session, Map.of(
            "type", "CONNECTION_ESTABLISHED",
            "message", "Connected to PetSave notifications",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", session.getId());
        String email = sessionIdToEmail.remove(session.getId());
        if (email != null) {
            userSessions.remove(email, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received WebSocket message: {}", message.getPayload());

        try {
            // Parse incoming message
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            // Handle different message types
            String type = (String) payload.get("type");
            switch (type) {
                case "PING":
                    sendMessage(session, Map.of(
                        "type", "PONG",
                        "timestamp", System.currentTimeMillis()
                    ));
                    break;
                default:
                    log.warn("Unknown message type: {}", type);
            }

        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage(), e);
            sendMessage(session, Map.of(
                "type", "ERROR",
                "message", "Failed to process message",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}",
                session.getId(), exception.getMessage(), exception);
        String email = sessionIdToEmail.remove(session.getId());
        if (email != null) {
            userSessions.remove(email, session);
        }
    }

    /**
     * Send message to specific session
     */
    public void sendMessage(WebSocketSession session, Object message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("Sent message to session {}: {}", session.getId(), jsonMessage);
            }
        } catch (Exception e) {
            log.error("Error sending message to session {}: {}",
                    session.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send a message to a specific user by email, if they're currently connected.
     * Silently no-ops if the user is offline — the caller is expected to have
     * already persisted the data, so offline delivery just means "picked up on
     * next fetch/reconnect" rather than lost.
     */
    public void sendToUser(String userEmail, Object message) {
        WebSocketSession session = userSessions.get(userEmail);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        } else {
            log.debug("User {} is not connected; message will be picked up on next fetch/reconnect", userEmail);
        }
    }

    /**
     * Broadcast message to all connected sessions
     */
    public void broadcastMessage(Object message) {
        userSessions.values().forEach(session -> sendMessage(session, message));
    }

    /**
     * Get number of active connections
     */
    public int getActiveConnectionCount() {
        return userSessions.size();
    }

    /**
     * Check if a user currently has an open WebSocket connection
     */
    public boolean isUserOnline(String userEmail) {
        WebSocketSession session = userSessions.get(userEmail);
        return session != null && session.isOpen();
    }

    private String extractEmail(WebSocketSession session) {
        try {
            String query = session.getUri() != null ? session.getUri().getQuery() : null;
            if (query == null) {
                return null;
            }
            String token = null;
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && kv[0].equals("token")) {
                    token = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    break;
                }
            }
            if (token == null || !jwtUtil.validateToken(token)) {
                return null;
            }
            return jwtUtil.extractUsername(token);
        } catch (Exception e) {
            log.warn("Failed to extract email from WebSocket handshake: {}", e.getMessage());
            return null;
        }
    }
}
