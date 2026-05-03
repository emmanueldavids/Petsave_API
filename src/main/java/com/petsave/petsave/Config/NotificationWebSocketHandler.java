package com.petsave.petsave.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);
        
        // Send welcome message
        sendMessage(session, Map.of(
            "type", "CONNECTION_ESTABLISHED",
            "message", "Connected to PetSave notifications",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", session.getId());
        sessions.remove(session.getId());
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
        sessions.remove(session.getId());
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
     * Broadcast message to all connected sessions
     */
    public void broadcastMessage(Object message) {
        sessions.values().forEach(session -> sendMessage(session, message));
    }

    /**
     * Get number of active connections
     */
    public int getActiveConnectionCount() {
        return sessions.size();
    }

    /**
     * Check if session is active
     */
    public boolean isSessionActive(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        return session != null && session.isOpen();
    }
}
