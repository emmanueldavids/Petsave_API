package com.petsave.petsave.Controller;

import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Service.ChatService;
import com.petsave.petsave.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {

    private final ChatService chatService;

    // Chat operations
    @GetMapping("/{userId}")
    public ResponseEntity<ChatResponse> getOrCreateChat(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/chats/{} - Getting or creating chat", userId);
        
        try {
            ChatResponse response = chatService.getOrCreateChat(userId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error getting/creating chat: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting/creating chat: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ChatResponse>> getUserChats(@AuthenticationPrincipal User currentUser) {
        log.info("GET /api/chats - Fetching user chats");
        
        try {
            List<ChatResponse> chats = chatService.getUserChats(currentUser);
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            log.error("Error fetching user chats: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ChatResponse>> searchChats(
            @RequestParam String query,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/chats/search - Searching chats with query: {}", query);
        
        try {
            List<ChatResponse> chats = chatService.searchChats(query, currentUser);
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            log.error("Error searching chats: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<List<ChatResponse>> getChatsWithUnreadMessages(@AuthenticationPrincipal User currentUser) {
        log.info("GET /api/chats/unread - Fetching chats with unread messages");
        
        try {
            List<ChatResponse> chats = chatService.getChatsWithUnreadMessages(currentUser);
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            log.error("Error fetching chats with unread messages: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{chatId}/read")
    public ResponseEntity<Void> markChatAsRead(
            @PathVariable Long chatId,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/chats/{}/read - Marking chat as read", chatId);
        
        try {
            chatService.markChatAsRead(chatId, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error marking chat as read: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error marking chat as read: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/chats/{} - Deleting chat", chatId);
        
        try {
            chatService.deleteChat(chatId, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error deleting chat: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting chat: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Message operations
    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/chats/messages - Sending message");
        
        try {
            MessageResponse response = chatService.sendMessage(request, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error sending message: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<MessageResponse>> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/chats/{}/messages - Fetching chat messages", chatId);
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<MessageResponse> messages = chatService.getChatMessages(chatId, pageable, currentUser);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            log.error("Error fetching chat messages: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching chat messages: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{chatId}/messages/recent")
    public ResponseEntity<List<MessageResponse>> getRecentMessages(
            @PathVariable Long chatId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/chats/{}/messages/recent - Fetching recent messages", chatId);
        
        try {
            List<MessageResponse> messages = chatService.getRecentMessages(chatId, currentUser);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            log.error("Error fetching recent messages: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching recent messages: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/messages")
    public ResponseEntity<Page<MessageResponse>> getUserMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/chats/messages - Fetching user messages");
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<MessageResponse> messages = chatService.getUserMessages(pageable, currentUser);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching user messages: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/messages/unread/count")
    public ResponseEntity<Long> getUnreadMessageCount(@AuthenticationPrincipal User currentUser) {
        log.info("GET /api/chats/messages/unread/count - Counting unread messages");
        
        try {
            Long count = chatService.getUnreadMessageCount(currentUser);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting unread messages: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<MessageResponse> markMessageAsRead(
            @PathVariable Long messageId,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/chats/messages/{}/read - Marking message as read", messageId);
        
        try {
            MessageResponse response = chatService.markMessageAsRead(messageId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error marking message as read: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error marking message as read: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/chats/messages/{} - Deleting message", messageId);
        
        try {
            chatService.deleteMessage(messageId, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error deleting message: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting message: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
