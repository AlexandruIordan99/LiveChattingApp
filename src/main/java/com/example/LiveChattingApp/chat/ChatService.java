package com.example.LiveChattingApp.chat;


import com.example.LiveChattingApp.user.UserRepository;
import com.example.LiveChattingApp.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatRepository chatRepository;
  private final UserRepository userRepository;
  private final ChatMapper mapper;

  @Transactional(readOnly = true)
  public List<ChatResponse> getChatsByReceiverId(Authentication currentUser) {
    final String userId = currentUser.getName();
    return chatRepository.findChatsByUserId(userId)
      .stream()
      .map(c -> mapper.toChatResponse(c, userId))
      .toList();
  }

  public String resolveReceiverIdFromChat(Chat chat, String senderId){
    return chat.getParticipants().stream()
      .filter(user-> user.getId().equals(senderId))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("Receiver not found"))
      .getId();
  }

  public Long createDirectChat(String senderId, String receiverId) {
    Optional<Chat> existingChat = chatRepository.findDirectChatBetweenUsers(senderId, receiverId);
    if (existingChat.isPresent()) {
      return existingChat.get().getId();
    }

    User sender = userRepository.findById(senderId)
      .orElseThrow(() -> new EntityNotFoundException("User with id " + senderId + " not found"));
    User receiver = userRepository.findById(receiverId)
      .orElseThrow(() -> new EntityNotFoundException("User with id " + receiverId + " not found"));

    Chat chat = Chat.builder()
      .type(ChatType.DIRECT)
      .creator(sender)
      .participants(Set.of(sender, receiver))
      .build();

    return chatRepository.save(chat).getId();
  }

  public Long createGroupChat(String creatorId, String chatName, Set<String> participantIds) {
    User creator = userRepository.findById(creatorId)
      .orElseThrow(() -> new EntityNotFoundException("Creator not found"));

    Set<User> participants = participantIds.stream()
      .map(id -> userRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("User not found: " + id)))
      .collect(Collectors.toSet());


    participants.add(creator);

    Chat chat = Chat.builder()
      .name(chatName)
      .type(ChatType.GROUP)
      .creator(creator)
      .participants(participants)
      .adminUserIds(Set.of(creatorId))
      .build();

    return chatRepository.save(chat).getId();
  }


  @Transactional
  public void addParticipantToGroup(Long chatId, String userId, String addedByUserId) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

    if (chat.getType() != ChatType.GROUP) {
      throw new IllegalArgumentException("Can only add participants to group chats");
    }

    if (!chat.isAdmin(addedByUserId)) {
      throw new IllegalArgumentException("Only admins can add participants");
    }

    if (chat.isParticipant(userId)) {
      throw new IllegalArgumentException("User is already a participant");
    }

    User userToAdd = userRepository.findById(userId)
      .orElseThrow(() -> new EntityNotFoundException("User not found"));

    Set<User> participants = new HashSet<>(chat.getParticipants());
    participants.add(userToAdd);
    chat.setParticipants(participants);

    chat.getParticipants().add(userToAdd);
    chatRepository.save(chat);
  }

  @Transactional
  public void removeParticipantFromGroup(Long chatId, String userId, String removedByUserId) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

    if (chat.getType() != ChatType.GROUP) {
      throw new IllegalArgumentException("Can only remove participants from group chats");
    }

    if (!userId.equals(removedByUserId) && !chat.isAdmin(removedByUserId)) {
      throw new IllegalArgumentException("Only admins can remove other participants");
    }

    User userToRemove = userRepository.findById(userId)
      .orElseThrow(() -> new EntityNotFoundException("User not found"));

    Set<User> participants = new HashSet<>(chat.getParticipants());
    participants.remove(userToRemove);
    chat.setParticipants(participants);

    Set<String> adminUserIds = new HashSet<>(chat.getAdminUserIds());
    adminUserIds.remove(userId);
    chat.setAdminUserIds(adminUserIds);

    chatRepository.save(chat);
  }

  @Transactional(readOnly = true)
  public Set<User> getChatParticipants(Long chatId) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
    return chat.getParticipants();
  }

  @Transactional(readOnly = true)
  public boolean isUserParticipant(Long chatId, String userId) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
    return chat.isParticipant(userId);
  }

  @Transactional
  public void makeUserAdmin(Long chatId, String userId, String requestedByUserId) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

    if (!chat.isAdmin(requestedByUserId)) {
      throw new IllegalArgumentException("Only admins can make other users admin");
    }

    if (!chat.isParticipant(userId)) {
      throw new IllegalArgumentException("User must be a participant to become admin");
    }

    Set<String> adminUserIds = new HashSet<>(chat.getAdminUserIds());
    adminUserIds.add(userId);
    chat.setAdminUserIds(adminUserIds);

    chatRepository.save(chat);
  }

  @Transactional
  public void removeAdminRole(Long chatId, String userId, String requestedByUserId) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

    if (!chat.isAdmin(requestedByUserId)) {
      throw new IllegalArgumentException("Only admins can remove admin privileges");
    }

    if (chat.getCreator().getId().equals(userId)) {
      throw new IllegalArgumentException("Cannot remove admin role from chat creator");
    }

    chat.getAdminUserIds().remove(userId);
    chatRepository.save(chat);
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

  public Chat findChatById(Long chatId){
    return chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found."));
  }

}
