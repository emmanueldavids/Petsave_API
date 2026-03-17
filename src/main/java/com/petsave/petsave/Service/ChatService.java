package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.*;
import com.petsave.petsave.Entity.Message.MessageType;
import com.petsave.petsave.Repository.*;
import com.petsave.petsave.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // Chat operations
    public ChatResponse getOrCreateChat(Long otherUserId, User currentUser) {
        log.info("Getting or creating chat between users {} and {}", currentUser.getId(), otherUserId);
        
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (otherUser.equals(currentUser)) {
            throw new RuntimeException("Cannot create chat with yourself");
        }
        
        Chat chat = chatRepository.findByUser1AndUser2OrUser2AndUser1(currentUser, otherUser)
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .user1(currentUser)
                            .user2(otherUser)
                            .build();
                    newChat = chatRepository.save(newChat);
                    log.info("Created new chat between users {} and {}", currentUser.getId(), otherUserId);
                    return newChat;
                });
        
        return convertToChatResponse(chat, currentUser);
    }

    public List<ChatResponse> getUserChats(User currentUser) {
        log.info("Fetching chats for user: {}", currentUser.getId());
        
        List<Chat> chats = chatRepository.findActiveChatsByUser(currentUser);
        return chats.stream()
                .map(chat -> convertToChatResponse(chat, currentUser))
                .collect(Collectors.toList());
    }

    public List<ChatResponse> searchChats(String search, User currentUser) {
        log.info("Searching chats for user {} with query: {}", currentUser.getId(), search);
        
        List<Chat> chats = chatRepository.searchChatsByUser(currentUser, search);
        return chats.stream()
                .map(chat -> convertToChatResponse(chat, currentUser))
                .collect(Collectors.toList());
    }

    public List<ChatResponse> getChatsWithUnreadMessages(User currentUser) {
        log.info("Fetching chats with unread messages for user: {}", currentUser.getId());
        
        List<Chat> chats = chatRepository.findChatsWithUnreadMessagesForUser(currentUser);
        return chats.stream()
                .map(chat -> convertToChatResponse(chat, currentUser))
                .collect(Collectors.toList());
    }

    public void markChatAsRead(Long chatId, User currentUser) {
        log.info("Marking chat {} as read for user: {}", chatId, currentUser.getId());
        
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        if (!chat.getUser1().equals(currentUser) && !chat.getUser2().equals(currentUser)) {
            throw new RuntimeException("Access denied");
        }
        
        chat.markAsRead(currentUser);
        chatRepository.save(chat);
        
        // Mark messages as read
        List<Message> unreadMessages = messageRepository.findUnreadMessagesInChat(chat, currentUser);
        unreadMessages.forEach(Message::markAsRead);
        messageRepository.saveAll(unreadMessages);
        
        log.info("Chat marked as read");
    }

    public void deleteChat(Long chatId, User currentUser) {
        log.info("Deleting chat {} for user: {}", chatId, currentUser.getId());
        
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        if (!chat.getUser1().equals(currentUser) && !chat.getUser2().equals(currentUser)) {
            throw new RuntimeException("Access denied");
        }
        
        chat.setIsActive(false);
        chatRepository.save(chat);
        
        log.info("Chat deleted successfully");
    }

    // Message operations
    public MessageResponse sendMessage(MessageRequest request, User currentUser) {
        log.info("Sending message from user {} to user {}", currentUser.getId(), request.getReceiverId());
        
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        if (receiver.equals(currentUser)) {
            throw new RuntimeException("Cannot send message to yourself");
        }
        
        // Get or create chat
        Chat chat = getOrCreateChatEntity(currentUser, receiver);
        
        // Create message
        Message message = Message.builder()
                .chat(chat)
                .sender(currentUser)
                .receiver(receiver)
                .content(request.getContent())
                .messageType(MessageType.valueOf(request.getMessageType()))
                .fileUrl(request.getFileUrl())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .build();
        
        message = messageRepository.save(message);
        
        // Update chat
        chat.updateLastMessage(request.getContent());
        chat.incrementUnreadCount(receiver);
        chatRepository.save(chat);
        
        // Update sender's online status
        currentUser.markAsOnline();
        userRepository.save(currentUser);
        
        log.info("Message sent successfully with ID: {}", message.getId());
        return convertToMessageResponse(message);
    }

    public Page<MessageResponse> getChatMessages(Long chatId, Pageable pageable, User currentUser) {
        log.info("Fetching messages for chat: {}", chatId);
        
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        if (!chat.getUser1().equals(currentUser) && !chat.getUser2().equals(currentUser)) {
            throw new RuntimeException("Access denied");
        }
        
        Page<Message> messages = messageRepository.findByChatAndIsDeletedFalseOrderByCreatedAtAsc(chat, pageable);
        return messages.map(this::convertToMessageResponse);
    }

    public List<MessageResponse> getRecentMessages(Long chatId, User currentUser) {
        log.info("Fetching recent messages for chat: {}", chatId);
        
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        if (!chat.getUser1().equals(currentUser) && !chat.getUser2().equals(currentUser)) {
            throw new RuntimeException("Access denied");
        }
        
        List<Message> messages = messageRepository.findLatestMessagesByChat(chat);
        return messages.stream()
                .limit(20) // Limit to 20 recent messages
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
    }

    public Page<MessageResponse> getUserMessages(Pageable pageable, User currentUser) {
        log.info("Fetching messages for user: {}", currentUser.getId());
        
        Page<Message> messages = messageRepository.findMessagesByUser(currentUser, pageable);
        return messages.map(this::convertToMessageResponse);
    }

    public Long getUnreadMessageCount(User currentUser) {
        log.info("Counting unread messages for user: {}", currentUser.getId());
        
        return messageRepository.countUnreadMessages(currentUser);
    }

    public MessageResponse markMessageAsRead(Long messageId, User currentUser) {
        log.info("Marking message {} as read for user: {}", messageId, currentUser.getId());
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        if (!message.getReceiver().equals(currentUser)) {
            throw new RuntimeException("Only receiver can mark message as read");
        }
        
        if (message.getIsRead()) {
            throw new RuntimeException("Message already read");
        }
        
        message.markAsRead();
        message = messageRepository.save(message);
        
        // Update chat unread count
        Chat chat = message.getChat();
        chat.markAsRead(currentUser);
        chatRepository.save(chat);
        
        log.info("Message marked as read");
        return convertToMessageResponse(message);
    }

    public void deleteMessage(Long messageId, User currentUser) {
        log.info("Deleting message {} for user: {}", messageId, currentUser.getId());
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        if (!message.getSender().equals(currentUser)) {
            throw new RuntimeException("Only sender can delete message");
        }
        
        message.setIsDeleted(true);
        messageRepository.save(message);
        
        log.info("Message deleted successfully");
    }

    // Helper methods
    private Chat getOrCreateChatEntity(User user1, User user2) {
        return chatRepository.findByUser1AndUser2OrUser2AndUser1(user1, user2)
                .orElseGet(() -> {
                    Chat chat = Chat.builder()
                            .user1(user1)
                            .user2(user2)
                            .build();
                    return chatRepository.save(chat);
                });
    }

    private ChatResponse convertToChatResponse(Chat chat, User currentUser) {
        User otherUser = chat.getOtherUser(currentUser);
        
        List<MessageResponse> recentMessages = messageRepository.findLatestMessagesByChat(chat)
                .stream()
                .limit(3) // Limit to 3 recent messages
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());

        return ChatResponse.builder()
                .id(chat.getId())
                .otherUser(convertToUserResponse(otherUser))
                .lastMessage(chat.getLastMessage())
                .unreadCount(chat.getUnreadCount(currentUser))
                .lastMessageAt(chat.getLastMessageAt())
                .lastMessageTimeAgo(chat.getLastMessageTimeAgo())
                .createdAt(chat.getCreatedAt())
                .isActive(chat.getIsActive())
                .recentMessages(recentMessages)
                .build();
    }

    private MessageResponse convertToMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .sender(convertToUserResponse(message.getSender()))
                .receiver(convertToUserResponse(message.getReceiver()))
                .content(message.getContent())
                .messageType(message.getMessageType().name())
                .fileUrl(message.getFileUrl())
                .fileName(message.getFileName())
                .fileSize(message.getFileSize())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .timeAgo(message.getTimeAgo())
                .readAt(message.getReadAt())
                .build();
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .location(user.getLocation())
                .avatarUrl(user.getAvatarUrl())
                .postsCount(user.getPostsCount())
                .petsAdopted(user.getPetsAdopted())
                .isOnline(user.getIsOnline())
                .lastSeen(user.getLastSeen())
                .lastSeenTimeAgo(user.getLastSeenTimeAgo())
                .joinedDate(user.getCreatedAt())
                .joinedTimeAgo(user.getCreatedAt() != null ? getTimeAgo(user.getCreatedAt()) : null)
                .build();
    }

    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (minutes < 1440) return (minutes / 60) + " hours ago";
        if (minutes < 10080) return (minutes / 1440) + " days ago";
        return (minutes / 10080) + " weeks ago";
    }
}
