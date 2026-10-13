package com.petsave.petsave.Service;

import com.petsave.petsave.Config.NotificationWebSocketHandler;
import com.petsave.petsave.Entity.ChatConversation;
import com.petsave.petsave.Entity.ChatMessage;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.ChatConversationRepository;
import com.petsave.petsave.Repository.ChatMessageRepository;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.dto.ChatResponse;
import com.petsave.petsave.dto.MessageResponse;
import com.petsave.petsave.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    public List<ChatResponse> listMyConversations() {
        User currentUser = getCurrentUser();
        return chatConversationRepository.findByParticipant(currentUser.getId()).stream()
                .map(c -> mapToResponse(c, currentUser))
                .collect(Collectors.toList());
    }

    public ChatResponse startConversation(Long participantId) {
        User currentUser = getCurrentUser();
        if (participantId.equals(currentUser.getId())) {
            throw new RuntimeException("You cannot start a conversation with yourself");
        }
        User otherUser = userRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + participantId));

        ChatConversation conversation = chatConversationRepository
                .findBetweenUsers(currentUser.getId(), otherUser.getId())
                .orElseGet(() -> {
                    ChatConversation c = new ChatConversation();
                    c.setParticipant1(currentUser);
                    c.setParticipant2(otherUser);
                    return chatConversationRepository.save(c);
                });

        return mapToResponse(conversation, currentUser);
    }

    public List<MessageResponse> getMessages(Long conversationId) {
        User currentUser = getCurrentUser();
        ChatConversation conversation = getConversationForParticipant(conversationId, currentUser);
        return chatMessageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    public MessageResponse sendMessage(Long conversationId, String content) {
        User currentUser = getCurrentUser();
        ChatConversation conversation = getConversationForParticipant(conversationId, currentUser);
        User otherUser = otherParticipant(conversation, currentUser);

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(currentUser);
        message.setContent(content);
        message.setIsRead(false);
        ChatMessage saved = chatMessageRepository.save(message);

        conversation.setLastMessageAt(saved.getCreatedAt());
        conversation.setLastMessagePreview(content.length() > 100 ? content.substring(0, 100) : content);
        chatConversationRepository.save(conversation);

        MessageResponse response = mapToMessageResponse(saved);

        webSocketHandler.sendToUser(otherUser.getEmail(), Map.of(
                "type", "CHAT_MESSAGE",
                "conversationId", conversation.getId(),
                "senderId", currentUser.getId(),
                "senderName", currentUser.getName(),
                "content", content,
                "timestamp", saved.getCreatedAt().toString()
        ));

        return response;
    }

    public MessageResponse editMessage(Long messageId, String newContent) {
        User currentUser = getCurrentUser();
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the sender can edit this message");
        }

        message.setContent(newContent);
        message.setEdited(true);
        ChatMessage saved = chatMessageRepository.save(message);

        ChatConversation conversation = saved.getConversation();
        refreshConversationPreview(conversation);

        User otherUser = otherParticipant(conversation, currentUser);
        webSocketHandler.sendToUser(otherUser.getEmail(), Map.of(
                "type", "MESSAGE_EDITED",
                "conversationId", conversation.getId(),
                "messageId", saved.getId(),
                "content", newContent,
                "timestamp", LocalDateTime.now().toString()
        ));

        return mapToMessageResponse(saved);
    }

    public void deleteMessage(Long messageId) {
        User currentUser = getCurrentUser();
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (!message.getSender().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("Only the sender can delete this message");
        }

        ChatConversation conversation = message.getConversation();
        User otherUser = otherParticipant(conversation, message.getSender());

        chatMessageRepository.delete(message);
        refreshConversationPreview(conversation);

        webSocketHandler.sendToUser(otherUser.getEmail(), Map.of(
                "type", "MESSAGE_DELETED",
                "conversationId", conversation.getId(),
                "messageId", messageId,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    private void refreshConversationPreview(ChatConversation conversation) {
        Optional<ChatMessage> latest = chatMessageRepository.findTopByConversationOrderByCreatedAtDesc(conversation);
        if (latest.isPresent()) {
            String content = latest.get().getContent();
            conversation.setLastMessagePreview(content.length() > 100 ? content.substring(0, 100) : content);
            conversation.setLastMessageAt(latest.get().getCreatedAt());
        } else {
            conversation.setLastMessagePreview(null);
            conversation.setLastMessageAt(null);
        }
        chatConversationRepository.save(conversation);
    }

    public void markAsRead(Long conversationId) {
        User currentUser = getCurrentUser();
        ChatConversation conversation = getConversationForParticipant(conversationId, currentUser);
        List<ChatMessage> unread = chatMessageRepository.findByConversationAndSenderNotAndIsReadFalse(conversation, currentUser);
        LocalDateTime now = LocalDateTime.now();
        for (ChatMessage message : unread) {
            message.setIsRead(true);
            message.setReadAt(now);
            chatMessageRepository.save(message);
        }
    }

    public long getUnreadCount() {
        User currentUser = getCurrentUser();
        return chatMessageRepository.countUnreadForUser(currentUser.getId());
    }

    private ChatConversation getConversationForParticipant(Long conversationId, User user) {
        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));
        boolean isParticipant = conversation.getParticipant1().getId().equals(user.getId())
                || conversation.getParticipant2().getId().equals(user.getId());
        if (!isParticipant) {
            throw new RuntimeException("You are not a participant in this conversation");
        }
        return conversation;
    }

    private User otherParticipant(ChatConversation conversation, User user) {
        return conversation.getParticipant1().getId().equals(user.getId())
                ? conversation.getParticipant2()
                : conversation.getParticipant1();
    }

    private ChatResponse mapToResponse(ChatConversation conversation, User currentUser) {
        User otherUser = otherParticipant(conversation, currentUser);
        long unread = chatMessageRepository.countByConversationAndSenderNotAndIsReadFalse(conversation, currentUser);

        return ChatResponse.builder()
                .id(conversation.getId())
                .otherUser(mapToUserResponse(otherUser))
                .lastMessage(conversation.getLastMessagePreview())
                .unreadCount((int) unread)
                .lastMessageAt(conversation.getLastMessageAt())
                .lastMessageTimeAgo(timeAgo(conversation.getLastMessageAt()))
                .createdAt(conversation.getCreatedAt())
                .isActive(true)
                .build();
    }

    private MessageResponse mapToMessageResponse(ChatMessage message) {
        User sender = message.getSender();
        User receiver = otherParticipant(message.getConversation(), sender);

        return MessageResponse.builder()
                .id(message.getId())
                .sender(mapToUserResponse(sender))
                .receiver(mapToUserResponse(receiver))
                .content(message.getContent())
                .isRead(message.getIsRead())
                .edited(message.getEdited())
                .createdAt(message.getCreatedAt())
                .timeAgo(timeAgo(message.getCreatedAt()))
                .readAt(message.getReadAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    private String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        long minutes = ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now());
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = hours / 24;
        if (days < 30) return days + (days == 1 ? " day ago" : " days ago");
        long months = days / 30;
        if (months < 12) return months + (months == 1 ? " month ago" : " months ago");
        long years = months / 12;
        return years + (years == 1 ? " year ago" : " years ago");
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Authentication required");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
