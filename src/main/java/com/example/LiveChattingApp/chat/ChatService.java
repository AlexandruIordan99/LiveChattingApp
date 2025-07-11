package com.example.LiveChattingApp.chat;

import com.example.LiveChattingApp.ChatParticipant.ChatParticipant;
import com.example.LiveChattingApp.ChatParticipant.ChatParticipantRepository;
import com.example.LiveChattingApp.ChatParticipant.ParticipantRole;
import com.example.LiveChattingApp.user.UserRepository;
import com.example.LiveChattingApp.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatRepository chatRepository;
  private final UserRepository userRepository;
  private final ChatParticipantRepository participantRepository;
  private final ChatMapper mapper;

  @Transactional(readOnly = true)
  public List<ChatResponse> getChatsByReceiverId(Authentication currentUser) {
    final String userId = currentUser.getName();
    return chatRepository.findChatsByUserId(userId)
      .stream()
      .map(c -> mapper.toChatResponse(c, userId))
      .toList();
  }

  public String createDirectChat(String senderId, String receiverId) {

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
      .build();

    Chat savedChat = chatRepository.save(chat);

    addParticipant(savedChat, sender, ParticipantRole.MEMBER, sender);
    addParticipant(savedChat, receiver, ParticipantRole.MEMBER, sender);

    return savedChat.getId();
  }

  private void addParticipant(Chat chat, User user, ParticipantRole role, User addedBy) {
    ChatParticipant participant = ChatParticipant.builder()
      .chat(chat)
      .user(user)
      .role(role)
      .addedBy(addedBy)
      .joinedAt(LocalDateTime.now())
      .isActive(true)
      .build();

    participantRepository.save(participant);
  }

  public String createGroupChat(String creatorId, String chatName, String description, Set<String> participantIds) {
    User creator = userRepository.findById(creatorId)
      .orElseThrow(() -> new EntityNotFoundException("Creator not found"));

    Chat chat = Chat.builder()
      .name(chatName)
      .type(ChatType.GROUP)
      .creator(creator)
      .build();

    Chat savedChat = chatRepository.save(chat);

    addParticipant(savedChat, creator, ParticipantRole.ADMIN, creator);

    participantIds.stream()
      .filter(id -> !id.equals(creatorId))
      .forEach(userId -> {
        User user = userRepository.findById(userId)
          .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        addParticipant(savedChat, user, ParticipantRole.MEMBER, creator);
      });

    return savedChat.getId();
  }

  @Transactional
  public void addParticipantToGroup(String chatId, String userId, String addedByUserId) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

    if (chat.getType() != ChatType.GROUP) {
      throw new IllegalArgumentException("Can only add participants to group chats");
    }

    ChatParticipant addedBy = participantRepository.findByChatIdAndUserId(chatId, addedByUserId)
      .orElseThrow(() -> new EntityNotFoundException("User not found in chat"));

    if (addedBy.getRole() != ParticipantRole.ADMIN) {
      throw new IllegalArgumentException("Only admins can add participants");
    }

    User userToAdd = userRepository.findById(userId)
      .orElseThrow(() -> new EntityNotFoundException("User not found"));

    if (participantRepository.findByChatIdAndUserId(chatId, userId).isPresent()) {
      throw new IllegalArgumentException("User is already a participant");
    }

    addParticipant(chat, userToAdd, ParticipantRole.MEMBER, addedBy.getUser());
  }

  @Transactional
  public void removeParticipantFromGroup(String chatId, String userId, String removedByUserId) {
    Chat chat = chatRepository.findById(chatId)
      .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

    if (chat.getType() != ChatType.GROUP) {
      throw new IllegalArgumentException("Can only remove participants from group chats");
    }

    ChatParticipant participant = participantRepository.findByChatIdAndUserId(chatId, userId)
      .orElseThrow(() -> new EntityNotFoundException("User not found in chat"));

    if (!userId.equals(removedByUserId)) {
      ChatParticipant removedBy = participantRepository.findByChatIdAndUserId(chatId, removedByUserId)
        .orElseThrow(() -> new EntityNotFoundException("User not found in chat"));

      if (removedBy.getRole() != ParticipantRole.ADMIN) {
        throw new IllegalArgumentException("Only admins can remove other participants");
      }
    }

    participant.setActive(false);
    participantRepository.save(participant);
  }

  @Transactional(readOnly = true)
  public List<ChatParticipant> getChatParticipants(String chatId) {
    return participantRepository.findActiveParticipantsByChatId(chatId);
  }

  @Transactional(readOnly = true)
  public boolean isUserParticipant(String chatId, String userId) {
    return participantRepository.findByChatIdAndUserId(chatId, userId).isPresent();
  }



}
