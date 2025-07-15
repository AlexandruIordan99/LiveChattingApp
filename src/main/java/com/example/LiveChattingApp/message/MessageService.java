package com.example.LiveChattingApp.message;

import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.chat.ChatService;
import com.example.LiveChattingApp.file.FileService;
import com.example.LiveChattingApp.file.FileUtils;
import com.example.LiveChattingApp.friendship.FriendshipService;
import com.example.LiveChattingApp.messageRequest.MessageRequest;
import com.example.LiveChattingApp.messageRequest.MessageRequestService;
import com.example.LiveChattingApp.notification.Notification;
import com.example.LiveChattingApp.notification.NotificationService;
import com.example.LiveChattingApp.notification.NotificationType;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final ChatRepository chatRepository;
  private final MessageMapper mapper;
  private final FileService fileService;
  private final NotificationService notificationService;
  private final FriendshipService friendshipService;
  private final MessageRequestService messageRequestService;
  private final ChatService chatService;

  public void saveMessage(MessageRequest messageRequest, Authentication authentication) {
    String senderId = authentication.getName();
    String receiverId = chatService.resolveReceiverIdFromChat(messageRequest.getChatId(), senderId);

    boolean areFriends = friendshipService.existsFriendshipBetweenUsers(senderId, receiverId);

    if (!areFriends) {
      messageRequestService.getOrCreateMessageRequest(messageRequest, senderId, receiverId);
      return;
    }

    Chat chat = chatRepository.findById(messageRequest.getChatId())
      .orElseThrow(() -> new RuntimeException("Chat not found"));

    User sender = userRepository.findById(messageRequest.getSenderId())
      .orElseThrow(() -> new RuntimeException("Sender not found"));

    Message message = Message.builder()
      .content(messageRequest.getFirstMessages().toString())
      .chat(chat)
      .sender(sender)
      .type(messageRequest.getType())
      .state(MessageState.SENT)
      .build();

    Message savedMessage = messageRepository.save(message);

    markAsRead(chat.getId(), sender.getId());
    sendNotificationsToParticipants(chat, savedMessage, sender);
  }

  @Transactional(readOnly = true)
  public List<MessageResponse> findChatMessages(String chatId, Authentication authentication) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

    if (!chat.isParticipant(authentication.getName())) {
      throw new RuntimeException("User is not a participant of this chat");
    }

    return messageRepository.findMessagesByChatId(chatId)
      .stream()
      .map(message -> mapper.toMessageResponse(message, authentication.getName()))
      .toList();
  }

  private void sendNotificationsToParticipants(Chat chat, Message message, User sender) {
    Set<User> participants = chat.getParticipants();

    participants.stream()
      .filter(p -> !p.getId().equals(sender.getId()))
      .forEach(p -> {
        Notification notification = Notification.builder()
          .chatId(chat.getId())
          .messageType(message.getType())
          .content(message.getContent())
          .senderId(sender.getId())
          .receiverId(p.getId())
          .type(NotificationType.MESSAGE)
          .chatName(chat.getChatName(p.getId()))
          .build();

        notificationService.sendNotification(p.getId(), notification);
      });
  }


  private void sendSeenNotifications(Chat chat, String userId) {
    Set<User> participants = chat.getParticipants();

    participants.stream()
      .filter(p -> !p.getId().equals(userId))
      .forEach(p -> {
        Notification notification = Notification.builder()
          .chatId(chat.getId())
          .type(NotificationType.SEEN)
          .receiverId(p.getId())
          .senderId(userId)
          .build();

        notificationService.sendNotification(p.getId(), notification);
      });
  }

  public void uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

    final String senderId = authentication.getName();
    User sender = userRepository.findById(senderId)
      .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

    final String filePath = fileService.saveFile(file, senderId);

    Message message = Message.builder()
      .sender(sender)
      .state(MessageState.SENT)
      .type(MessageType.IMAGE)
      .mediaFilePath(filePath)
      .chat(chat)
      .build();

    Message savedMessage = messageRepository.save(message);


    Set<User> participants = chat.getParticipants();

    participants.stream()
      .filter(p -> !p.getId().equals(senderId))
      .forEach(p -> {
        Notification notification = Notification.builder()
          .chatId(chat.getId())
          .type(NotificationType.IMAGE)
          .senderId(senderId)
          .receiverId(p.getId())
          .messageType(MessageType.IMAGE)
          .media(FileUtils.readFileFromLocation(filePath))
          .build();

        notificationService.sendNotification(p.getId(), notification);
      });
  }

  public void markAsRead(String chatId, String userId) {
    Chat chat = chatRepository.findById(chatId).orElseThrow();
    chat.getLastReadTimestamps().put(userId, LocalDateTime.now());

    chatRepository.save(chat);
  }

  public long getUnreadCount(String chatId, String userId) {
    Chat chat = chatRepository.findById(chatId).orElseThrow();
    LocalDateTime lastRead = chat.getLastReadTimestamps().get(userId);

    if (lastRead == null) return chat.getMessages().size();

    return chat.getMessages().stream()
      .filter(m -> m.getCreatedDate().isAfter(lastRead))
      .filter(m -> !m.getSender().getId().equals(userId)) // Don't count own messages
      .count();
  }


  @Transactional(readOnly = true)
  public boolean isChatRead(String chatId, String userId) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

    if (!chat.isParticipant(userId)) {
      throw new RuntimeException("User is not a participant of this chat");
    }

    LocalDateTime lastRead = chat.getLastReadTimestamps().get(userId);

    if (lastRead == null) {
      return chat.getMessages().isEmpty();
    }

    return chat.getMessages().stream()
      .filter(m -> !m.getSender().getId().equals(userId))
      .noneMatch(m -> m.getCreatedDate().isAfter(lastRead));
  }


}


