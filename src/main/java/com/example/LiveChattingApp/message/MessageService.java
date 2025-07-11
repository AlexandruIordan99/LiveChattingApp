package com.example.LiveChattingApp.message;


import com.example.LiveChattingApp.ChatParticipant.ChatParticipant;
import com.example.LiveChattingApp.ChatParticipant.ChatParticipantRepository;
import com.example.LiveChattingApp.MessageReadStatus.MessageReadStatus;
import com.example.LiveChattingApp.MessageReadStatus.MessageReadStatusRepository;
import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.file.FileService;
import com.example.LiveChattingApp.file.FileUtils;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final ChatRepository chatRepository;
  private final ChatParticipantRepository participantRepository;
  private final MessageReadStatusRepository readStatusRepository;
  private final MessageMapper mapper;
  private final FileService fileService;
  private final NotificationService notificationService;

  public void saveMessage(MessageRequest messageRequest, Authentication authentication) {
    Chat chat = chatRepository.findById(messageRequest.getChatId())
      .orElseThrow(() -> new RuntimeException("Chat not found"));

    User sender = userRepository.findById(messageRequest.getSenderId())
      .orElseThrow(() -> new RuntimeException("Sender not found"));

    Message message = Message.builder()
      .content(messageRequest.getContent())
      .chat(chat)
      .sender(sender)
      .type(messageRequest.getType())
      .state(MessageState.SENT)
      .build();

    Message savedMessage = messageRepository.save(message);

    createReadStatusesForMessage(savedMessage, sender.getId());
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

  private void createReadStatusesForMessage(Message message, String senderId) {
    List<ChatParticipant> participants = participantRepository
      .findActiveParticipantsByChatId(message.getChat().getId());

    List<MessageReadStatus> readStatuses = participants.stream()
      .filter(p -> !p.getUser().getId().equals(senderId))
      .map(p -> MessageReadStatus.builder()
        .message(message)
        .user(p.getUser())
        .isRead(false)
        .build())
      .collect(Collectors.toList());

    readStatusRepository.saveAll(readStatuses);
  }

  private void sendNotificationsToParticipants(Chat chat, Message message, User sender) {
    List<ChatParticipant> participants = participantRepository
      .findActiveParticipantsByChatId(chat.getId());

    participants.stream()
      .filter(p -> !p.getUser().getId().equals(sender.getId()))
      .forEach(p -> {
        Notification notification = Notification.builder()
          .chatId(chat.getId())
          .messageType(message.getType())
          .content(message.getContent())
          .senderId(sender.getId())
          .receiverId(p.getUser().getId())
          .type(NotificationType.MESSAGE)
          .chatName(chat.getChatName(p.getUser().getId()))
          .build();

        notificationService.sendNotification(p.getUser().getId(), notification);
      });
  }

  @Transactional
  public void setMessagesToRead(String chatId, Authentication authentication) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

    String userId = authentication.getName();

    if (!chat.isParticipant(userId)) {
      throw new RuntimeException("User is not a participant of this chat");
    }

    // Use the repository method to mark messages as read
    readStatusRepository.markMessagesAsRead(chatId, userId, LocalDateTime.now());

    // Send notifications to other participants
    sendSeenNotifications(chat, userId);
  }

  private void sendSeenNotifications(Chat chat, String userId) {
    List<ChatParticipant> participants = participantRepository
      .findActiveParticipantsByChatId(chat.getId());

    participants.stream()
      .filter(p -> !p.getUser().getId().equals(userId))
      .forEach(p -> {
        Notification notification = Notification.builder()
          .chatId(chat.getId())
          .type(NotificationType.SEEN)
          .receiverId(p.getUser().getId())
          .senderId(userId)
          .build();

        notificationService.sendNotification(p.getUser().getId(), notification);
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

    createReadStatusesForMessage(savedMessage, senderId);

    List<ChatParticipant> participants = participantRepository
      .findActiveParticipantsByChatId(chatId);

    participants.stream()
      .filter(p -> !p.getUser().getId().equals(senderId))
      .forEach(p -> {
        Notification notification = Notification.builder()
          .chatId(chat.getId())
          .type(NotificationType.IMAGE)
          .senderId(senderId)
          .receiverId(p.getUser().getId())
          .messageType(MessageType.IMAGE)
          .media(FileUtils.readFileFromLocation(filePath))
          .build();

        notificationService.sendNotification(p.getUser().getId(), notification);
      });
  }

  @Transactional(readOnly = true)
  public long getUnreadMessageCount(String chatId, String userId) {
    return messageRepository.countUnreadMessagesByChatIdAndUserId(chatId, userId);
  }

  @Transactional(readOnly = true)
  public List<MessageReadStatus> getMessageReadStatuses(String messageId) {
    return readStatusRepository.findReadStatusesByMessageId(messageId);
  }

}


