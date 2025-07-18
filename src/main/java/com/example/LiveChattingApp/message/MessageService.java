package com.example.LiveChattingApp.message;

import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.chat.ChatService;
import com.example.LiveChattingApp.chat.ChatType;
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
import java.util.*;
import java.util.stream.Collectors;

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

  public void sendDirectMessage(String senderId, String receiverId, Long chatId, String content) {
    if (content == null || content.trim().isEmpty()) {
      throw new IllegalArgumentException("Message content cannot be empty.");
    }

    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found."));

    User sender = userRepository.findById(senderId)
      .orElseThrow(() -> new RuntimeException("Sender not found"));

    if (chat.getType() != ChatType.DIRECT) {
      throw new RuntimeException("Send direct message should only be used for direct chats.");
    }

    boolean areFriends = friendshipService.existsFriendshipBetweenUsers(senderId, receiverId);

    Message message = Message.builder()
      .chat(chat)
      .sender(sender)
      .content(content)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .build();

    if (areFriends) {
      messageRepository.save(message);
    } else {
      Optional<MessageRequest> existingRequest = messageRequestService.findExistingRequest(senderId, receiverId);

      MessageRequest request;
      if (existingRequest.isPresent()) {
        request = existingRequest.get();
      } else {
        request = messageRequestService.createMessageRequest(senderId, chatId);
      }

      messageRequestService.addToFirstMessages(request.getId(), message);
      Message savedMessage = messageRepository.save(message);

      markAsRead(chat.getId(), sender.getId());
      sendNotificationsToParticipants(chat, savedMessage, sender);
    }

  }

  public void sendGroupMessage(String senderId, Long chatId, String content) {
    if (content == null || content.trim().isEmpty()) {
      throw new IllegalArgumentException("Message content cannot be empty.");
    }

    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(()-> new EntityNotFoundException("Chat not found."));

    if(chat.getType() != ChatType.GROUP){
      throw  new RuntimeException("Send group message is only allowed for group chats.");
    }

    User sender = userRepository.findById(senderId)
      .orElseThrow(() -> new EntityNotFoundException("Sender not found."));

    Set<User> chatParticipants = chat.getParticipants();
    User chatCreator = chat.getCreator();
    String chatCreatorId = chatCreator.getId();

    boolean senderIsParticipant = chatParticipants.stream()
      .anyMatch(participant -> participant.getId().equals(senderId));

    if (!senderIsParticipant) {
      throw new RuntimeException("Sender is not a participant in this chat.");
    }

    boolean senderIsCreator = chat.getCreator().getId().equals(senderId);

    Message message = Message.builder()
      .chat(chat)
      .sender(sender)
      .content(content)
      .type(MessageType.TEXT)
      .state(MessageState.SENT)
      .build();

    Message savedMessage = messageRepository.save(message);

    markAsRead(chat.getId(), senderId);

    for (User currentParticipant : chatParticipants) {
      if(Objects.equals(currentParticipant.getId(), senderId)){
        continue;
      }

      boolean areFriends = friendshipService.existsFriendshipBetweenUsers(chatCreatorId, currentParticipant.getId());

       if (senderIsCreator && !areFriends) {
        MessageRequest request = messageRequestService
          .findExistingRequest(chatCreatorId, currentParticipant.getId())
          .orElseGet(() -> messageRequestService.createMessageRequest(chatCreatorId, chatId));

        messageRequestService.addToFirstMessages(request.getId(), message);
        savedMessage = messageRepository.save(message);

        markAsRead(chat.getId(), senderId);

      } else {
        savedMessage = messageRepository.save(message);

        markAsRead(chat.getId(), currentParticipant.getId());
      }
    }

    sendNotificationsToParticipants(chat, savedMessage, chatCreator);


  }

  public String getMessageReceiverId(String senderId, Long chatId){
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(()-> new EntityNotFoundException("Chat not found"));

    if(chat.getType() !=ChatType.DIRECT){
      throw new RuntimeException("Please use the get participants method for group chats.");
    }


    User receiver = chat.getParticipants()
      .stream()
      .filter(p -> !p.getId().equals(senderId))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("Receiver not found in the chat."));

    return receiver.getId();
  }

  public Set<User> getGroupChatParticipantsExceptSender(Long chatId, String senderId){
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(()-> new EntityNotFoundException("Chat not found"));

    if(chat.getType() !=ChatType.GROUP){
      throw new RuntimeException("Please use the get receiver method for direct chats.");
    }

    Set<User> participants = chat.getParticipants();

    return participants.stream()
      .filter(p -> !p.getId().equals(senderId))
      .collect(Collectors.toSet());

  }

  @Transactional(readOnly = true)
  public List<MessageResponse> findChatMessages(Long chatId, Authentication authentication) {
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

  public void uploadMediaMessage(Long chatId, MultipartFile file, Authentication authentication) {
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

  public void markAsRead(Long chatId, String userId) {
    Chat chat = chatRepository.findById(chatId).orElseThrow();
    chat.getLastReadTimestamps().put(userId, LocalDateTime.now());

    chatRepository.save(chat);
  }

  public long getUnreadCount(Long chatId, String userId) {
    Chat chat = chatRepository.findById(chatId).orElseThrow();
    LocalDateTime lastRead = chat.getLastReadTimestamps().get(userId);

    if (lastRead == null) return chat.getMessages().size();

    return chat.getMessages().stream()
      .filter(m -> m.getCreatedDate().isAfter(lastRead))
      .filter(m -> !m.getSender().getId().equals(userId)) // Don't count own messages
      .count();
  }


  @Transactional(readOnly = true)
  public boolean isChatRead(Long chatId, String userId) {
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


