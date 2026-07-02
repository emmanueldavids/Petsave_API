package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.ChatService;
import com.petsave.petsave.dto.ChatMessageRequest;
import com.petsave.petsave.dto.ChatResponse;
import com.petsave.petsave.dto.MessageResponse;
import com.petsave.petsave.dto.StartConversationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ChatResponse>> listConversations() {
        return ResponseEntity.ok(chatService.listMyConversations());
    }

    @PostMapping("/conversations")
    public ResponseEntity<ChatResponse> startConversation(@Valid @RequestBody StartConversationRequest request) {
        return ResponseEntity.ok(chatService.startConversation(request.getParticipantId()));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getMessages(id));
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable Long id, @Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(id, request.getContent()));
    }

    @PatchMapping("/conversations/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        chatService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Conversation marked as read"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        return ResponseEntity.ok(Map.of("unreadCount", chatService.getUnreadCount()));
    }
}
